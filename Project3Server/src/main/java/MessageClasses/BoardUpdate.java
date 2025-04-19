package MessageClasses;

public class BoardUpdate extends Message{
    private static final long serialVersionUID = 42L;
    private final Piece[][] gameBoard; // 6×7 board of EMPTY/PLAYER1/PLAYER2
    private final boolean isYourTurn;

    public BoardUpdate(Piece[][] newBoard, boolean isYourTurn) {
        gameBoard = newBoard;
        this.isYourTurn = isYourTurn;
    }

    public Piece[][] getGameBoard() {
        return this.gameBoard;
    }
    public boolean isItMyTurn() {
        return this.isYourTurn;
    }
}
