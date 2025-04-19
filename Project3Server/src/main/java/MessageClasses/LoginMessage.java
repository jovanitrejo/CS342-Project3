package MessageClasses;

public class LoginMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final String username;

    public LoginMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
