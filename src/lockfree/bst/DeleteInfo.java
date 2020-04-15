package lockfree.bst;

public class DeleteInfo extends Info {

    public Internal gParent;
    public Update pupdate;

    public DeleteInfo(Leaf leaf, Internal parent, Internal gParent, Update update) {
        super(leaf, parent);
        this.gParent = gParent;
        this.pupdate = update;
    }
}
