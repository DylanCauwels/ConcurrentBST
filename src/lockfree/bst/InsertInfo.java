package lockfree.bst;

public class InsertInfo extends Info {
    public Internal newInternal;

    public InsertInfo(Leaf leaf, Internal parent, Internal add) {
        super(leaf, parent);
        newInternal = add;
    }
}
