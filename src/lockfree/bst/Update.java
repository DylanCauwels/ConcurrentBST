package lockfree.bst;

public class Update {

    enum State {
        CLEAN,
        DFLAG,
        IFLAG,
        MARK
    }

    public State state;
    public Info info;

    public Update(State state, Info info) {
        this.state = state;
        this.info = info;
    }
}
