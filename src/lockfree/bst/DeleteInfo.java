package lockfree.bst;

import java.util.concurrent.atomic.AtomicStampedReference;

// Info class containing all the fields that a process needs to complete a delete operation
public class DeleteInfo extends Info {

    public Internal gParent;
    public AtomicStampedReference<Info> pupdate;

    public DeleteInfo(Leaf leaf, Internal parent, Internal gParent, AtomicStampedReference<Info> passedPupdate) {
        super(leaf, parent);
        this.gParent = gParent;
        this.pupdate = passedPupdate;
    }
}
