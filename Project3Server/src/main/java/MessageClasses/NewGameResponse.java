package MessageClasses;

public class NewGameResponse extends Message{
    private static final long serialVersionUID = 42L;
    private boolean isInQueue = true;
    private String opponent;
    private boolean isRed;
    private boolean isYourTurn;

    public NewGameResponse() {}

    public NewGameResponse(String opponent, boolean isRed, boolean isYourTurn) {
        this.isInQueue = false;
        this.opponent = opponent;
        this.isRed = isRed;
        this.isYourTurn = isYourTurn;
    }

    public boolean isInQueue() {
        return this.isInQueue;
    }

    public String getOpponentUsername() {
        return this.opponent;
    }

    public boolean amIRed() {
        return this.isRed;
    }

    public boolean isItMyTurn() {
        return this.isYourTurn;
    }
}
