package contexts;

import MessageClasses.Piece;
import java.util.Arrays;
import javafx.scene.paint.Color;

public class GameState {
    private boolean isYourTurn;
    private final boolean isRed;            // assigned color
    private Piece[][] gameBoard; // 6×7 board of EMPTY/PLAYER1/PLAYER2
    private final Piece myPiece; // PLAYER1 or PLAYER2
    private final Piece oppPiece;
    private final Color myColor;
    private final Color oppColor;

    public GameState(boolean isRed, boolean isYourTurn, int mySlot) {
        this.isRed = isRed;
        this.isYourTurn = isYourTurn;
        this.gameBoard  = new Piece[6][7];
        for (Piece[] row : gameBoard) {
            Arrays.fill(row, Piece.EMPTY);
        }

        this.myPiece = (mySlot == 1 ? Piece.PLAYER1 : Piece.PLAYER2);
        this.oppPiece = (mySlot == 1 ? Piece.PLAYER2 : Piece.PLAYER1);

        this.myColor  = isRed
                ? Color.rgb(200,25,25)   // red
                : Color.rgb(200,175,25); // yellow
        this.oppColor = isRed
                ? Color.rgb(200,175,25)
                : Color.rgb(200,25,25);
        // …
    }

    public Piece   getMyPiece()   { return myPiece; }
    public Piece   getOppPiece()  { return oppPiece; }
    public Color   getMyColor()   { return myColor; }
    public Color   getOppColor()  { return oppColor; }

    public Piece[][] getGameBoard() {return this.gameBoard;}

    public void updateBoard(Piece[][] board) {
        this.gameBoard = board;
        System.out.println("Successfully updated board");
    }

    /**
     * Find the first empty slot from the bottom in this column.
     * @throws IllegalArgumentException if the column is full.
     */
    public int findFreeRow(int column) {
        for (int r = gameBoard.length - 1; r >= 0; r--) {
            if (gameBoard[r][column] == Piece.EMPTY) {
                return r;
            }
        }
        throw new IllegalArgumentException("Column " + column + " is full");
    }

    /**
     * Drop the current player's piece into the given column,
     * flip both the turn flag and the piece‐color flag,
     * and return the row it landed in.
     */
    public void applyMove(int column) {
        int row = findFreeRow(column);
        gameBoard[row][column] = myPiece;
    }

    public boolean isYourTurn() { return isYourTurn; }
    public void setIsYourTurn(boolean isYourTurn) {
        System.out.println("It is " + (isYourTurn ? "" : "NOT") + " your turn");
        this.isYourTurn = isYourTurn;
    }

    public boolean amIRed() {
        return this.isRed;
    }
}