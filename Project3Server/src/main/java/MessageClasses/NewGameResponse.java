package MessageClasses;

public class NewGameResponse extends Message{
    private static final long serialVersionUID = 42L;
    private final boolean isInQueue;
    private final String opponent;
    private final boolean isRed;
    private final boolean isYourTurn;
    private final int playerSlot;

    public NewGameResponse(String opponent, boolean isRed, boolean isYourTurn, int playerSlot) {
        this.isInQueue = false;
        this.opponent = opponent;
        this.isRed = isRed;
        this.isYourTurn = isYourTurn;
        this.playerSlot = playerSlot;
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

    public int getPlayerSlot() {
        return this.playerSlot;
    }
}
