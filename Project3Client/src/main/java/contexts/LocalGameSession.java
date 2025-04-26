package contexts;

import MessageClasses.*;
import javafx.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalGameSession {
    private final Piece[][] board = new Piece[6][7];
    public boolean player1Turn = true;
    private int totalMoves = 0;
    private final Instant startTime;

    public LocalGameSession() {
        this.startTime = Instant.now();
        for (Piece[] row : board) {
            Arrays.fill(row, Piece.EMPTY);
        }
    }

    public void onMove(int row, int col) {
        boolean successfulMove = attemptMove(row, col);
        if (!successfulMove) return;
        totalMoves++;
        Pair<List<int[]>, String> winningPieces = findWinningPositions(row, col);
        if (winningPieces.getKey() != null) {
            // The game found a winning move, end the game here and notify both clients.
            Instant endTime = Instant.now();
            long minutes = Duration.between(startTime, endTime).toMinutes();
            // To do
            return;
        }
        System.out.println("Checking for draw...");
        if(isBoardFull()) {
            Instant endTime = Instant.now();
            long minutes = Duration.between(startTime, endTime).toMinutes();
            // To do
            return;
        }
        this.player1Turn = !player1Turn;
        System.out.println("Sending new board to players!");
    }

    private boolean attemptMove(int row, int col) {
        if(board[row][col] != Piece.EMPTY) {
            return false;
        } else {
            Piece attempter = player1Turn ? Piece.PLAYER1 : Piece.PLAYER2;
            board[row][col] = attempter;
            return true;
        }
    }

    public boolean isBoardFull() {
        for (Piece[] row : board) {
            for (Piece p : row) {
                if (p == Piece.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<int[]> scanLine(int row, int col,
                                 int dr1, int dc1,
                                 int dr2, int dc2,
                                 Piece me)
    {
        List<int[]> cells = new ArrayList<>();
        // include the piece just placed
        cells.add(new int[]{row, col});

        // scan 1st direction
        int r = row + dr1, c = col + dc1;
        scan(dr1, dc1, me, cells, r, c);

        // scan opposite direction
        r = row + dr2; c = col + dc2;
        scan(dr2, dc2, me, cells, r, c);

        return (cells.size() >= 4) ? cells : null;
    }

    private void scan(int dr1, int dc1, Piece me, List<int[]> cells, int r, int c) {
        while (r >= 0 && r < board.length &&
                c >= 0 && c < board[0].length &&
                board[r][c] == me)
        {
            cells.add(new int[]{r, c});
            r += dr1; c += dc1;
        }
    }

    public Pair<List<int[]>, String> findWinningPositions(int row, int col) {
        Piece me = board[row][col];
        // horizontal
        List<int[]> win = scanLine(row, col, 0, 1,  0, -1, me);
        if (win != null) return new Pair<>(win, "Horizontal");
        // vertical
        win = scanLine(row, col, 1, 0,  -1, 0, me);
        if (win != null) return new Pair<>(win, "Vertical");
        // “\” diagonal
        win = scanLine(row, col, 1, 1,  -1, -1, me);
        if (win != null) return new Pair<>(win, "Diagonal");
        // “/” diagonal
        win = scanLine(row, col, 1, -1,  -1, 1, me);
        return new Pair<>(win, "Diagonal");
    }

    public Piece[][] getBoard() {
        return this.board;
    }

    public int getTotalMoves() {
        return this.totalMoves;
    }
}
