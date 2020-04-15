package lockfree.bst;

// superclass for IInfo and DInfo
public class Info {
    public Leaf leaf;
    public Internal parent;

    public Info(Leaf leaf, Internal parent) {
        this.leaf = leaf;
        this.parent = parent;
    }
}
