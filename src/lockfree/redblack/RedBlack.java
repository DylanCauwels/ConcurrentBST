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
        Node z;
        // restart point
        while (true) {
            z = root.parent;
            while (!root.flag.compareAndSet(false, true)) ;
            Node y = this.root;
            while (!y.nil) {
                z = y;
                y = (key < y.key) ? y.left : y.right;
                if (!y.flag.compareAndSet(false, true)) {
                    z.flag.set(false);
                    break; //restart
                }
                if (!y.nil) z.flag.set(false);
            }
            if(!setupLocalAreaForInsert(z)) {
                z.flag.set(false);
            } else {
                break;
            }
        }
        Node x = new Node(RED, key, z);
        x.flag.set(true);
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

    }

    // try to get flags for rest of the local area for insert
    private boolean setupLocalAreaForInsert(Node z) {
        Node parent = z.parent;
        // try and flag the parent
        if (!parent.flag.compareAndSet(false, true)) {
            return false;
        }
        // ensure the parent has not been changed
        if (parent != z.parent) {
            parent.flag.set(false);
            return false;
        }
        // grab and flag the sibling of the target
        Node sibling = (parent.left == z) ? parent.right : parent.left;
        if (!sibling.flag.compareAndSet(false, true)) {
            parent.flag.set(false);
            return false;
        }
        // ensure sibling has not been changes
        // TODO: not in pseudocode, experimental
        if (((parent.left == z) ? parent.right : parent.left) != sibling) {
            parent.flag.set(false);
            sibling.flag.set(false);
            return false;
        }
        // try to get the flags and markers above the target
        if (!getFlagsAndMarkersAbove(z.parent, z)) {
            parent.flag.set(false);
            sibling.flag.set(false);
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
                    x = moveInserterUp(x);
                // parent's sibling is not RED and requires a rebalance
                } else {
                    // CASE 2 -- new node is right child, rotate the subtree under x's parents left to compensate
                    if (x == x.parent.right) {
                        // done to allow CASE 3 code to run properly as not setting x to x.parent would bump the ancestry up by 1
                        x = x.parent;
                        leftRotate(x);
                    }
                    // CASE 3 -- now rotate the grandparent tree to rebalance
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
                    x = moveInserterUp(x);
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

    private Node moveInserterUp(Node x) {
        return x;
    }

    // rebalance the tree under x with +1 to left side and -1 to right side (counter-clockwise rotation)
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

    // rebalance the tree under x with +1 to right side and -1 to left side (clockwise rotation)
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
