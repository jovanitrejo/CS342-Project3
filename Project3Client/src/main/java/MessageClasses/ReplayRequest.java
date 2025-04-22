package MessageClasses;

public class ReplayRequest extends Message {
    private static final long serialVersionUID = 42L;
    private String requestorUsername = "";

    public ReplayRequest() {}

    public ReplayRequest(String requestorUsername) {
        this.requestorUsername = requestorUsername;
    }

    public String getRequestorUsername() {
        return this.requestorUsername;
    }
}
