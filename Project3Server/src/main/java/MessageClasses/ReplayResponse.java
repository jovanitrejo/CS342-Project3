package MessageClasses;

public class ReplayResponse extends Message {
    private static final long serialVersionUID = 42L;
    private final boolean accepted;

    public ReplayResponse(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return this.accepted;
    }
}
