package MessageClasses;

public class LoginResponse extends Message {
    private static final long serialVersionUID = 42L;
    private final boolean successful;
    private final String description;

    public LoginResponse(boolean wasSuccessful, String description) {
        this.successful = wasSuccessful;
        this.description = description;
    }

    public boolean getWasSuccessful() {
        return this.successful;
    }

    public String getDescription() {
        return this.description;
    }
}
