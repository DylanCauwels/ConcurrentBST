package lockfree.redblack;

import java.util.concurrent.atomic.AtomicReference;

public class Node {
    public int key;
    public Node left;
    public Node right;
    public Node parent;
    public Color color;

    public boolean nil;

    public AtomicReference<Boolean> flag = new AtomicReference<>(false);

    public enum Color {
        RED, BLACK
    }

    public Node(Color color, int key, Node parent) {
        this.color = color;
        this.nil = false;
        this.parent = parent;
        this.key = key;
        this.left = new Node(this);
        this.right = new Node(this);
    }

    // constructor for making nil Nodes
    public Node(Node parent) {
        this.nil = true;
        color = Color.BLACK;
        this.parent = parent;
    }

    public Node grandparent() {
        return this.parent.parent;
    }
}
