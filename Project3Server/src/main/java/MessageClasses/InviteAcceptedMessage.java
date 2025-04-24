package MessageClasses;

public class InviteAcceptedMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final String opponent;

    public InviteAcceptedMessage(String opponent) {
        this.opponent = opponent;
    }

    public String getOpponent() {
        return this.opponent;
    }
}
