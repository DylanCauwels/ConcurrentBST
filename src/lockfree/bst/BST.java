package lockfree.bst;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class BST {
    private AtomicMarkableReference<Node> root;

    public BST() {
        root = new AtomicMarkableReference<>(null, false);
    }

    // T if insert succeeded
    // F is key already present in tree
    public boolean insert(int key) {
        Node node = new Node(key, null, null);
        boolean mark = root.isMarked();
        // empty tree if root is marked
        // we will have to deal with chance that loc changes
        if (mark) {
            Node loc = root.getReference();
            root.compareAndSet(loc, node, mark, false);
        }
        return false;
    }

    // T if key in tree
    // F if not
    public boolean contains(int key) {
        return false;
    }

    // T if key found and deleted
    // F if key not present in tree
    public boolean delete(int key) {
        return false;
    }

    // inner class representing a single node of the tree
    class Node {
        public int key;
        public AtomicMarkableReference<Children> children;

        public Node(int key, Node left, Node right) {
            this.key = key;
            children = new AtomicMarkableReference<>(new Children(left, right), false);
        }

        // inner class to represent both children with a single pointer for CAS operations
        class Children {
            public Node left, right;

            public Children(Node left, Node right) {
                this.left = left;
                this.right = right;
            }
        }
    }
}
