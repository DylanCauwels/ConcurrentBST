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
        return false;
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
        Node gp = null, p = null;
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

    private void helpDelete(DeleteInfo op) {

    }

    private void helpMarked(DeleteInfo op) {

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
