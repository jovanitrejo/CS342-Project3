package MessageClasses;

public class InviteDeniedMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final String opponent;

    public InviteDeniedMessage(String opponent) {
        this.opponent = opponent;
    }

    public String getOpponent() {
        return this.opponent;
    }
}
