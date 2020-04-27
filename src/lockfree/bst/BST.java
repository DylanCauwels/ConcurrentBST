package lockfree.bst;

import java.util.concurrent.atomic.AtomicStampedReference;

public class BST implements util.TreeInterface {
    // dummy node values
    private int dummyOne = Integer.MAX_VALUE - 1;
    private int dummyTwo = Integer.MAX_VALUE;

    // flag values
    private int CLEAN = 1;
    private int DFLAG = 2;
    private int IFLAG = 3;
    private int MARK = 4;

    // root node
    private Internal root;

    /**
     * constructor, initializes the tree with three dummy nodes such that search operations will still succeed and won't
     * return with null pointers for grandparent or parent values (necessary for insertions and deletes)
     */
    public BST() {
        // initialization of dummy nodes
        root = new Internal(dummyTwo, new Leaf(dummyOne), new Leaf(dummyTwo),
                null, CLEAN);
    }

    /**
     *
     * @param key the value to be inserted into the tree
     * @return true when the insert succeeds, false if the key is already present
     */
    public boolean insert(int key) {
        Internal p, newInternal;
        Leaf l, newSibling;
        Leaf newLeaf = new Leaf(key);
        AtomicStampedReference<Info> pupdate;
        InsertInfo op;

        while (true) {
            // search for insertion point
            SearchReturn s = search(key);
            p = s.parent;
            l = s.l;
            pupdate = s.pupdate;
            // check for key already in tree
            if (l.key == key) {
                return false;
            // check that the parent node is clean
            } else if (pupdate.getStamp() != CLEAN) {
                help(pupdate);
            // start insertion process
            } else {
                // setup internal node and InsertInfo
                newSibling = new Leaf(l.key);
                Node left, right;
                if (newLeaf.key < newSibling.key) {
                    left = newLeaf;
                    right = newSibling;
                } else {
                    left = newSibling;
                    right = newLeaf;
                }
                newInternal = new Internal(Integer.max(key, l.key), left, right, null, CLEAN);
                op = new InsertInfo(l, p, newInternal);
                // try to mark the parent node for insertion
                if (pupdate.compareAndSet(null, op, CLEAN, IFLAG)) {
                    // ensure leaf nodes haven't been modified between the search and the CAS
                    if (!(op.parent.left.get() == op.leaf || op.parent.right.get() == op.leaf)) {
                        pupdate.compareAndSet(op, null, IFLAG, CLEAN);
                        continue;
                    }
                    helpInsert(op);
                    return true;
                // help the node finish its operation
                } else {
                    help(pupdate);
                }
            }
        }
    }

    /**
     * a search method for values within the tree
     * @param key the value you want to search for in the tree
     * @return true if the key is in the tree false if not
     */
    public boolean contains(int key) {
        SearchReturn s = search(key);
        return s.l.key == key;
    }

    /**
     * delete goes through the tree and searches for a leaf node with the passed in key, once it finds the node in question
     * it marks its parent and grandparent and then replaces the parent with the deleted node's sibling
     * @param key the value to be deleted from the tree
     * @return false if the key is not contained in the tree and true once the operation succeeds
     */
    public boolean delete(int key) {
        AtomicStampedReference<Info> pupdate, gpupdate;
        DeleteInfo op;

        while (true) {
            // check that the key we're trying to delete is in the tree
            SearchReturn nodeSearch = search(key);
            if (nodeSearch.l.key != key) return false;
            // if grandparent state not clean, help out
            if (nodeSearch.gpupdate.getStamp() != CLEAN) {
                help(nodeSearch.gpupdate);
            }
            // if parent state not clean, help out
            else if (nodeSearch.pupdate.getStamp() != CLEAN) {
                help(nodeSearch.pupdate);
            }
            // ancestors clean, attempt the delete
            else {
                pupdate = nodeSearch.pupdate;
                gpupdate = nodeSearch.gpupdate;
                // create new Info tag notifying giving helping ability to other processes
                op = new DeleteInfo(nodeSearch.l, nodeSearch.parent, nodeSearch.gParent, pupdate);
                if (gpupdate.compareAndSet(null, op, CLEAN, DFLAG)) {
                    // try to mark the parent then delete, on failure retry entire process
                    if (helpDelete(op)) {
                        return true;
                    }
                } else {
                    help(gpupdate);
                }
            }
        }
    }

    /**
     * allows the BST object to be more easily used in console printing or logging
     * @return a String representing the BST object
     */
    public String toString() {
        return this.inOrder(root);
    }

    /**
     * a method to show the inorder traversal of a tree
     * @param node the first node to start with, generally the root
     * @return a String representing the inorder traversal of the tree
     */
    private String inOrder(Node node) {
        String displayNodes = "";
        if (node != null) {
            if (node instanceof Internal) {
                displayNodes = displayNodes +
                        this.inOrder(((Internal) node).left.get());
                displayNodes = displayNodes +
                        this.inOrder(((Internal) node).right.get());
            } else {
                if (node.key != dummyOne && node.key != dummyTwo)
                    displayNodes = displayNodes + node.toString() + "\n";
                else
                    displayNodes = displayNodes + "";
            }
        }
        return displayNodes;
    }

    /**
     * a class meant to hold the result of a tree search operation
     */
    class SearchReturn {
        public Internal parent;
        public Internal gParent;
        public Leaf l;
        public AtomicStampedReference<Info> gpupdate, pupdate;

        public SearchReturn(Internal gParent, Internal parent, Leaf l,
                            AtomicStampedReference<Info> pupdate, AtomicStampedReference<Info> gpupdate) {
            this.gParent = gParent;
            this.parent = parent;
            this.l = l;
            this.gpupdate = gpupdate;
            this.pupdate = pupdate;
        }
    }

    /**
     * called for an insertion, deletion, or contains operation and contains references to parent and grandparent nodes
     * so delete and insert can succeed
     * @param key the value that is being searched for
     * @return a SearchReturn object with the necessary values for an operation
     */
    private SearchReturn search(int key) {
        Node gp = null, p = null;
        Node l = this.root;
        AtomicStampedReference<Info> gpup = null, pup = null;

        // search through the tree while the "leaf" field is still an internal node
        while (l instanceof Internal) {
            gp = p;
            p = l;
            gpup = pup;
            pup = ((Internal) p).update;
            l = key < l.key ? ((Internal) p).left.get() : ((Internal) p).right.get();
        }

        // return SearchReturn object with casted nodes
        return new SearchReturn((Internal) gp, (Internal) p, (Leaf) l, pup, gpup);
    }

    /**
     * called when a node is flagged and another process is trying to help it complete its action, will go through
     * the possible flag options and call the appropriate helper method if it is true
     * @param u the update field of the contested node
     */
    private void help(AtomicStampedReference u) {
        if (u.getStamp() == IFLAG) helpInsert((InsertInfo) u.getReference());
        else if (u.getStamp() == MARK) helpMarked((DeleteInfo) u.getReference());
        else if (u.getStamp() == DFLAG) helpDelete((DeleteInfo) u.getReference());
    }

    /**
     * inserts a new internal node under a parent as referenced in the op InsertInfo object
     * @param op the InsertInfo object used to complete the operation
     */
    private void helpInsert(InsertInfo op) {
        if (op == null) return;
        CASChild(op.parent, op.leaf, op.newInternal);
        op.parent.update.compareAndSet(op, null, IFLAG, CLEAN);
    }

    /**
     * attempts to mark the parent node for removal and continue with deletion, otherwise aborts and returns
     * @param op the DeleteInfo object used to complete the operation
     * @return true if the final flag marking succeeds or false if a retry is necessary
     */
    private boolean helpDelete(DeleteInfo op) {
        // attempt to mark the parent and continue with deletion
        if (op.parent.update.compareAndSet(null, op, CLEAN, MARK)) {
            helpMarked(op);
            return true;
        // abort and restart deletion
        } else {
            help(op.parent.update);
            op.gParent.update.compareAndSet(op, null, DFLAG, CLEAN);
            return false;
        }
    }

    /**
     * deletes the marked node and rearranges the tree to be correct again
     * @param op the DeleteInfo object used to complete the operation
     */
    private void helpMarked(DeleteInfo op) {
        // grab node that isn't the leaf Node to be deleted
        Node other = (op.parent.left.get().key == op.leaf.key) ? op.parent.right.get() : op.parent.left.get();
        // replace internal parent node with sibling of removed node
        CASChild(op.gParent, op.parent, other);
        // remove flag from grandparent
        op.gParent.update.compareAndSet(op.gParent.update.getReference(), null, DFLAG, CLEAN);
    }

    /**
     * a method to replace an old child node with a new child node to either insert or remove a node
     * @param parent the internal node the insertion is being performed under
     * @param old the old sibling node that will be replaced
     * @param newNode the new sibling node going to be inserted
     */
    private void CASChild(Internal parent, Node old, Node newNode) {
        if (newNode.key < parent.key) {
            parent.left.compareAndSet(old, newNode);
        } else {
            parent.right.compareAndSet(old, newNode);
        }
    }
}