package MessageClasses;

public class InviteUserMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final String username;
    public InviteUserMessage(String username) { this.username = username; }

    public String getUsername() {
        return this.username;
    }
}
