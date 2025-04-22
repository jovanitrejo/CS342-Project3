package MessageClasses;

public class ReplayRequest extends Message {
    private static final long serialVersionUID = 42L;
    private final String requestorUsername;

    public ReplayRequest(String requestorUsername) {
        this.requestorUsername = requestorUsername;
    }

    public String getRequestorUsername() {
        return this.requestorUsername;
    }
}
