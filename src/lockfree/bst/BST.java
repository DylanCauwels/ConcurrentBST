package lockfree.bst;

import java.util.concurrent.atomic.AtomicStampedReference;

public class BST {
    // dummies necessary for unchanging 3 nodes of tree
    private int dummyOne = Integer.MAX_VALUE - 1;
    private int dummyTwo = Integer.MAX_VALUE;

    private int CLEAN = 1;
    private int DFLAG = 2;
    private int IFLAG = 3;
    private int MARK = 4;

    private Internal root;

    public BST() {
        // initialization of dummy tree
        root = new Internal(dummyTwo, new Leaf(dummyOne), new Leaf(dummyTwo),
                                null, CLEAN);

    }

    // T if insert succeeded
    // F is key already present in tree
    public boolean insert(int key) {
        Internal p, newInternal;
        Leaf l, newSibling;
        Leaf newLeaf = new Leaf(key);
        AtomicStampedReference<Info> pupdate, oldResult;
        InsertInfo op;

        while(true) {
            SearchReturn s = search(key);
            p = s.parent;
            l = s.l;
            pupdate = s.pupdate;
            if (l.key == key) return false;
            if (pupdate.getStamp() != CLEAN) help(pupdate);
            else {
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
                oldResult = p.update;
                if (p.update.compareAndSet(pupdate.getReference(), op, pupdate.getStamp(), IFLAG)) {
                    helpInsert(op);
                    return true;
                } else {
                    help(oldResult);
                }
            }
        }
    }

    // T if key in tree
    // F if not
    public boolean contains(int key) {
        SearchReturn s = search(key);
        return s.l.key == key;
    }

    // T if key found and deleted
    // F if key not present in tree
    public boolean delete(int key) {
        Internal gp, p;
        Leaf l;
        AtomicStampedReference<Info> pupdate, gpupdate, result;
        DeleteInfo op;

        while (true) {
            // check that the key we're trying to delete is in the tree
            SearchReturn nodeSearch = search(key);
            if (nodeSearch.l.key != key) return false;
            // if grandparent state not clean, help out
            if (nodeSearch.gpupdate.getStamp() != CLEAN)  {
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
                // create new Info tag notifying other threads of imminent deletion
                op = new DeleteInfo(nodeSearch.l, nodeSearch.parent, (Internal)nodeSearch.gParent, pupdate.getReference(), pupdate.getStamp());
                if (gpupdate.compareAndSet(pupdate.getReference(), op, pupdate.getStamp(), DFLAG)) {
                    // try to mark the parent then delete, on failure retry entire process
                    if (helpDelete(op)) {
                        return true;
                    }
                } else {
                    help(pupdate);
                }
            }
        }
    }

    public String toString() {
        return this.inOrder(root);
    }

    private String inOrder(Node node) {
        String displayNodes = "";
        if (node != null) {
            if (node instanceof Internal) {
                displayNodes = displayNodes +
                        this.inOrder(((Internal)node).left.get());
//                displayNodes = displayNodes + node.toString() + "\n";
                displayNodes = displayNodes +
                        this.inOrder(((Internal)node).right.get());
            } else {
                if (node.key != dummyOne && node.key != dummyTwo)
                    displayNodes = displayNodes + node.toString() + "\n";
                else
                    displayNodes = displayNodes + "";
            }
        }
        return displayNodes;
    }


    /** PRIVATE HELPERS **/

    class SearchReturn {
        public Internal parent;
        public Node gParent;
        public Leaf l;
        public AtomicStampedReference<Info> gpupdate, pupdate;

        public SearchReturn(Node gParent, Internal parent, Leaf l,
                            AtomicStampedReference<Info> pupdate, AtomicStampedReference<Info> gpupdate) {
            this.gParent = gParent;
            this.parent = parent;
            this.l = l;
            this.gpupdate = gpupdate;
            this.pupdate = pupdate;
        }
    }

    private SearchReturn search(int key) {
        // p points to encapsulating internal node
        Node gp = null, p = null;
        // l will eventually point to leaf node
        Node l = this.root;
        AtomicStampedReference<Info> gpup = null, pup = null;

        while (l instanceof Internal) {
            gp = p;
            p = l;
            gpup = pup;
            pup = ((Internal)p).update;
            // traverse
            l = key < l.key ? ((Internal) p).left.get() : ((Internal) p).right.get();
        }

        // if the key of l is not dummyOne, then GP is also an Internal
        return new SearchReturn(gp, (Internal) p, (Leaf) l, pup, gpup);
    }

    private void help(AtomicStampedReference u) {
        if (u != null) {
            if (u.getStamp() == IFLAG) helpInsert((InsertInfo) u.getReference());
            else if (u.getStamp() == MARK) helpMarked((DeleteInfo) u.getReference());
            else if (u.getStamp() == DFLAG) helpDelete((DeleteInfo) u.getReference());
        }
    }

    private void helpInsert(InsertInfo op) {
        CASChild(op.parent, op.leaf, op.newInternal);
        op.parent.update.compareAndSet(op, op, IFLAG, CLEAN);
    }

    private boolean helpDelete(DeleteInfo op) {
        // TODO: not sure if im getting the EXPECTED value or the CURRENT value for the parent, changed  from op.parent.update.getStamp() to CLEAN
        if (op.parent.update.compareAndSet(op.parent.update.getReference(), op.pupdate.getReference(), CLEAN, MARK)) {
            // CAS success, finish deletion
            helpMarked(op);
            return true;
        // CAS failed, help the WIP parent finish then remove the flag on the grandparent to try again
        } else {
            help(op.parent.update);
            op.gParent.update.compareAndSet(op.gParent.update.getReference(), op.gParent.update.getReference(), DFLAG, CLEAN);
            return false;
        }

    }

    private void helpMarked(DeleteInfo op) {
        // grab node that isn't the leaf Node to be deleted
        Node other = (op.parent.left.get().key == op.leaf.key) ? op.parent.left.get() : op.parent.right.get();
        // replace internal parent node with sibling of removed node
        CASChild(op.gParent, op.parent, other);
        // remove flag from grandparent
        op.gParent.update.compareAndSet(op.gParent.update.getReference(), op.gParent.update.getReference(), DFLAG, CLEAN);
    }

    private void CASChild(Internal parent, Node old, Node newNode) {
        if (parent != null && newNode != null) {
            if (newNode.key < parent.key) {
                parent.left.compareAndSet(old, newNode);
            } else {
                parent.right.compareAndSet(old, newNode);
            }
        }
    }

//    // inner class representing a single node of the tree
//    class Node {
//        public int key;
//        public AtomicMarkableReference<Children> children;
//
//        public Node(int key, Node left, Node right) {
//            this.key = key;
//            children = new AtomicMarkableReference<>(new Children(left, right), false);
//        }
//
//        // inner class to represent both children with a single pointer for CAS operations
//        class Children {
//            public Node left, right;
//
//            public Children(Node left, Node right) {
//                this.left = left;
//                this.right = right;
//            }
//        }
//    }
}
