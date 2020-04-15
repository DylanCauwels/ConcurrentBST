package lockfree.bst;

import java.util.concurrent.atomic.AtomicReference;

public class Internal extends Node {
    public AtomicReference<Node> left, right;
    public AtomicReference<Update> update;


    public Internal(int key, Node left, Node right, Update update) {
        super(key);
        this.left = new AtomicReference<>(left);
        this.right = new AtomicReference<>(right);
        this.update = new AtomicReference<>(update);
    }
}
