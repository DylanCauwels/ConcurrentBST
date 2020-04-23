package lockfree.redblack;

import static lockfree.redblack.Node.Color.BLACK;
import static lockfree.redblack.Node.Color.RED;

/**
 * PROPERTIES
 * every node is either red or black
 * the root node is black
 * external nodes are black
 * a red nodes children are both black
 * all paths from a node to its leaf descendants contain the same number of black nodes
 *
 * left child is less than parent
 * right child is greater than or equal to parent
 */
public class RedBlack {
    private Node root;

    public RedBlack() {
        this.root = new Node(new Node(null));
    }

    public void insert(int key) {
        Node z = root.parent;
        Node y = this.root;
        while (!y.nil) {
            z = y;
            y = (key < y.key) ? y.left : y.right;
        }
        Node x = new Node(RED, key, z);
        // z is the nil root parent, the node is the first one in the tree
        if (z == root.parent) {
            this.root = x;
            z.left = x;
        } else if (key < z.key) {
            z.left = x;
        } else {
            z.right = x;
        }
        insertFixup(x);

//        while (true) {
//            Node z = root;
//            while (!z.flag.compareAndSet(false, true)) ;
//            Node y = this.root;
//
//            while (!y.nil) {
//                z = y;
//                y = (key < y.key) ? y.left.get() : y.right.get();
//                // y has been modified, release flag and restart search
//                if (!y.flag.compareAndSet(true, false)) {
//                    z.flag.compareAndSet(true, false);
//                    break;
//                }
//                if (!y.nil) {
//                    z.flag.compareAndSet(true, false);
//                }
//            }
//
//            Node x = new Node(RED, key);
//            // try to fetch local area flags
//            if (!setupLocalAreaForInsert(z)) {
//                // couldn't fetch local area flags, retry
//                z.flag.compareAndSet(false, true);
//                break;
//            }
//
//            // place new node as a child of z
//            if (z == this.root) {
//                // TODO i have no clue here
//            } else if (key < z.key) {
//                z.left = x;
//            } else {
//                z.right = x;
//            }
//
//            insertFixup(x);
//            break;
//        }
    }

    // try to get flags for rest of the local area for insert
    private boolean setupLocalAreaForInsert(Node target, Node parent) {
        // try and flag the parent
        if (!parent.flag.compareAndSet(false, true)) {
            return false;
        }
        // ensure the parent has not been changed
        if (parent != target.parent) {
            parent.flag.compareAndSet(true, false);
            return false;
        }
        // grab and flag the sibling of the target
        Node sibling = (parent.left == target) ? parent.right : parent.left;
        if (!sibling.flag.compareAndSet(false, true)) {
            parent.flag.compareAndSet(true, false);
            return false;
        }
        // TODO: add saved values to check if the sibling is the same before and after marked
        // try to get the flags and markers above the target
        if (!getFlagsAndMarkersAbove(parent, target)) {
            parent.flag.compareAndSet(true, false);
            sibling.flag.compareAndSet(true, false);
            return false;
        }
        return true;
    }

    private boolean getFlagsAndMarkersAbove(Node parent, Node target) {
        Thread.currentThread().getId(); // for marking
        return false;
    }

    private void getFlagsForMarkers() {

    }

    private void releaseFlags() {

    }

    private void insertFixup(Node x) {
        while (x.parent.color == RED) {
            // parent is left sibling
            if (x.parent == x.grandparent().left) {
                Node y = x.grandparent().right;
                // CASE 1 -- both siblings are red, we can set them both to black and move the red node up to grandparent
                if (y.color == RED) {
                    x.parent.color = BLACK;
                    y.color = BLACK;
                    x.grandparent().color = RED;
                    x = x.parent.parent;
                } else {
                    // CASE 2
                    if (x == x.parent.right) {
                        x = x.parent;
                        leftRotate(x);
                    }
                    // CASE 3
                    x.parent.color = BLACK;
                    x.grandparent().color = RED;
                    rightRotate(x.grandparent());
                }
            // parent is right sibling
            } else {
                Node y = x.grandparent().left;
                // CASE 1
                if (y.color == RED) {
                    x.parent.color = BLACK;
                    y.color = BLACK;
                    x.grandparent().color = RED;
                    x = x.parent.parent;
                } else {
                    // CASE 2
                    if (x == x.parent.left) {
                        x = x.parent;
                        rightRotate(x);
                    }
                    // CASE 3
                    x.parent.color = BLACK;
                    x.grandparent().color = RED;
                    leftRotate(x.grandparent());
                }
            }
        }
        root.color = BLACK;
    }

    private void moveInserterUp() {

    }

    private void leftRotate(Node x) {
        Node y = x.right;
        // connecting y.left and x.right
        x.right = y.left;
        y.left.parent = x;
        // connecting y.parent and x.parent
        y.parent = x.parent;
        if (x == root) {
            // FIXME: make an extension class for the root parent with a single child st it can be checked with instanceof
            root = y;
            x.parent.left = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        // connecting y.left and x.parent
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(Node x) {
        Node y = x.left;
        // connecting y.right and x.left
        x.left = y.right;
        y.right.parent = x;
        // connecting y.parent and x.parent
        y.parent = x.parent;
        if (x == root) {
            // FIXME: make an extension class for the root parent with a single child st it can be checked with instanceof
            root = y;
            x.parent.left = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        // connecting y.right and x.parent
        y.right = x;
        x.parent = y;
    }

    public boolean contains(int key) {
        Node curr = this.root;
        while (!curr.nil) {
            if (curr.key == key) return true;
            else curr = (key < curr.key) ? curr.left : curr.right;
        }
        return false;
    }

    // for testing purposes
    public Node getRoot() {
        return this.root;
    }
}
