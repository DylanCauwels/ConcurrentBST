package lockfree.bst;

public class Node {
    public int key;

    public Node(int k) {
        key = k;
    }

    public String toString() {
        return "" + key;
    }

    public int getKey() {
        return key;
    }
}
