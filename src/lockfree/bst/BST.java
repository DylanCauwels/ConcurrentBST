package lockfree.bst;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

public class BST {
    // dummies necessary for unchanging 3 nodes of tree
    private int dummyOne = Integer.MAX_VALUE - 1;
    private int dummyTwo = Integer.MAX_VALUE;

    private Internal root;

    public BST() {
        // initialization of dummy tree
        root = new Internal(dummyTwo, new Leaf(dummyOne), new Leaf(dummyTwo),
                                new Update(Update.State.CLEAN, null));
    }

    // T if insert succeeded
    // F is key already present in tree
    public boolean insert(int key) {
        Internal p, newInternal;
        Leaf l, newSibling;
        Leaf newLeaf;
        Update pupdate, result;
        InsertInfo op;

        while(true) {
            SearchReturn s = search(key);
            p = s.parent;
            l = s.l;
            pupdate = s.pupdate;
            if (l.key == key) return false;
            if (pupdate.state != Update.State.CLEAN) help(pupdate);
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


    /** PRIVATE HELPERS **/

    class SearchReturn {
        public Internal parent;
        public Node gParent;
        public Leaf l;
        public Update gpupdate, pupdate;

        public SearchReturn(Node gParent, Internal parent, Leaf l,
                            Update gpupdate, Update pupdate) {
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
        Update gpup = null, pup = null;

        while (l instanceof Internal) {
            gp = p;
            p = l;
            gpup = pup;
            pup = ((Internal) p).update.get();
            // traverse
            l = key < l.key ? ((Internal) p).left.get() : ((Internal) p).right.get();
        }

        // if the key of l is not dummyOne, then GP is also an Internal
        return new SearchReturn(gp, (Internal) p, (Leaf) l, pup, gpup);
    }

    private void help(Update u) {
        if (u != null) {
            if (u.state == Update.State.IFLAG) helpInsert((InsertInfo) u.info);
            else if (u.state == Update.State.MARK) helpMarked((DeleteInfo) u.info);
            else if (u.state == Update.State.DFLAG) helpDelete((DeleteInfo) u.info);
        }
    }

    private void helpInsert(InsertInfo op) {
        CASChild(op.parent, op.leaf, op.newInternal);
//        op.parent.update.compareAndSet(new Update())
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
