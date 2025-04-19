package MessageClasses;

import java.util.List;

public class WinMessage extends Message{
    private static final long serialVersionUID = 42L;
    private final String winnerUsername;
    private final List<int[]> winningPieces;

    public WinMessage(String winnerUsername, List<int[]> winningPieces) {
        this.winnerUsername = winnerUsername;
        this.winningPieces = winningPieces;
    }

    public String getWinnerUsername() {
        return this.winnerUsername;
    }

    public List<int[]> getWinningPieces() {
        return this.winningPieces;
    }
}
