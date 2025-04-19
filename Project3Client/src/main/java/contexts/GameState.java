package contexts;

import MessageClasses.Piece;
import javafx.application.Platform;

import java.util.Arrays;
import java.util.List;

public class GameState {
    private final String opponent;
    private boolean isYourTurn;
    private final boolean isRed;            // assigned color
    private Piece[][] gameBoard; // 6×7 board of EMPTY/PLAYER1/PLAYER2

    public GameState(String opponent, boolean isRed, boolean isYourTurn) {
        this.opponent   = opponent;
        this.isRed      = isRed;
        this.isYourTurn = isYourTurn;
        this.gameBoard  = new Piece[6][7];
        for (Piece[] row : gameBoard) {
            Arrays.fill(row, Piece.EMPTY);
        }
    }

    public Piece[][] getGameBoard() {return this.gameBoard;}

    public void updateBoard(Piece[][] board) {
        this.gameBoard = board;
        System.out.println("Successfully updated board");
    }

    /**
     * Find the first empty slot from the bottom in this column.
     * @throws IllegalArgumentException if the column is full.
     */
    private int findFreeRow(int column) {
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
    public int applyMove(int column) {
        int row = findFreeRow(column);
        gameBoard[row][column] = isRed
                ? Piece.PLAYER1
                : Piece.PLAYER2;
        return row;
    }

    public boolean isYourTurn() { return isYourTurn; }
    public void setIsYourTurn(boolean isYourTurn) {
        System.out.println("It is " + (isYourTurn ? "" : "NOT") + " your turn");
        this.isYourTurn = isYourTurn;
    }
    public String getOpponent(){ return opponent;   }
}