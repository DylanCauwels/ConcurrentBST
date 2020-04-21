package lockfree.bst;

// Info class containing all the fields that a process needs to complete an insert operation
public class InsertInfo extends Info {
    public Internal newInternal;

    public InsertInfo(Leaf leaf, Internal parent, Internal add) {
        super(leaf, parent);
        newInternal = add;
    }
}
