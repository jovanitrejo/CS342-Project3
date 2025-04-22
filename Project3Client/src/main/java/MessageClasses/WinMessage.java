package MessageClasses;

import java.util.List;

public class WinMessage extends Message{
    private static final long serialVersionUID = 42L;
    private final boolean youWon;
    private final List<int[]> winningPieces;
    private final long lengthInMinutes;
    private final int totalMoves;
    private final String winType;

    public WinMessage(Boolean youWon, List<int[]> winningPieces, long lengthInMinutes, int totalMoves, String winType) {
        this.winType = winType;
        this.youWon = youWon;
        this.winningPieces = winningPieces;
        this.lengthInMinutes = lengthInMinutes;
        this.totalMoves = totalMoves;
    }

    public boolean didIWin() {
        return this.youWon;
    }
    public List<int[]> getWinningPieces() {
        return this.winningPieces;
    }
    public long getLengthInMinutes() { return this.lengthInMinutes; }
    public int getTotalMoves() { return this.totalMoves; }
    public String getWinType() { return this.winType; }
}
