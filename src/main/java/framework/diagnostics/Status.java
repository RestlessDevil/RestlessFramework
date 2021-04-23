package framework.diagnostics;

public final class Status {

    public enum State {
        uninitialized, operational, malfunction, busy
    }

    private final State state;
    private final Exception exception;

    public Status(State state, Exception exception) {
        this.state = state;
        this.exception = exception;
    }

    public State getState() {
        return state;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isOperational() {
        return (state == State.operational);
    }

    public boolean isMalfunction() {
        return (state == State.malfunction);
    }

    public boolean isUninitialized() {
        return (state == State.uninitialized);
    }

}
