package lockfree.bst;

// used to let other processes help complete operations on nodes they need to access
// superclass for IInfo and DInfo
public class Info {
    public Leaf leaf;
    public Internal parent;

    public Info(Leaf leaf, Internal parent) {
        this.leaf = leaf;
        this.parent = parent;
    }
}
