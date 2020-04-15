package lockfree.bst;

import java.util.concurrent.atomic.AtomicStampedReference;

public class DeleteInfo extends Info {

    public Internal gParent;
    public AtomicStampedReference<Info> pupdate;

    public DeleteInfo(Leaf leaf, Internal parent, Internal gParent, Info updateInfo, int updateState) {
        super(leaf, parent);
        this.gParent = gParent;
        this.pupdate = new AtomicStampedReference<>(updateInfo, updateState);
    }
}
