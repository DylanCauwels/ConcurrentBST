package lockfree.bst;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class Internal extends Node {
    public AtomicReference<Node> left, right;
    public AtomicStampedReference<Info> update;


    public Internal(int key, Node left, Node right, Info updateInfo, int updateState) {
        super(key);
        this.left = new AtomicReference<>(left);
        this.right = new AtomicReference<>(right);
        this.update = new AtomicStampedReference<>(updateInfo, updateState);
    }
}
