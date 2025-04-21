package MessageClasses;

import java.util.List;

public class WinMessage extends Message{
    private static final long serialVersionUID = 42L;
    private final boolean youWon;
    private final List<int[]> winningPieces;

    public WinMessage(Boolean youWon, List<int[]> winningPieces) {
        this.youWon = youWon;
        this.winningPieces = winningPieces;
    }

    public boolean didIWin() {
        return this.youWon;
    }

    public List<int[]> getWinningPieces() {
        return this.winningPieces;
    }
}
