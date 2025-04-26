import MessageClasses.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class GameSession implements Runnable {
    private Thread gameThread;
    private final Piece[][] board = new Piece[6][7];
    ArrayList<String> chat = new ArrayList<>();
    Server.ClientThread player1;
    Server.ClientThread player2;
    Server.ClientThread currentPlayer;
    private final Instant startTime;
    private int totalMoves = 0;
    private final Consumer<String> guiLogger;

    public GameSession(Server.ClientThread player1, Server.ClientThread player2, Consumer<String> guiLogger) {
        this.startTime = Instant.now();
        this.player1 = player1;
        this.player2 = player2;
        this.guiLogger = guiLogger;

        // pick a random color for player1, and invert for player2
        boolean p1IsRed  = ThreadLocalRandom.current().nextBoolean();
        boolean p2IsRed  = !p1IsRed;

        // pick a random starter, and invert for the other
        boolean p1Starts = ThreadLocalRandom.current().nextBoolean();
        boolean p2Starts = !p1Starts;

        if (p1Starts) {
            currentPlayer = player1;
        } else {
            currentPlayer = player2;
        }
        // Initialize the board
        for (Piece[] pieces : board) {
            Arrays.fill(pieces, Piece.EMPTY);
        }

        // notify both clients
        player1.sendMessage(new NewGameResponse(
                player2.getUsername(),   // opponent name
                p1IsRed,                 // isRed?
                p1Starts,                 // isYourTurn?
                1
        ));
        player2.sendMessage(new NewGameResponse(
                player1.getUsername(),
                p2IsRed,
                p2Starts,
                2
        ));
    }

    public void onMove(MoveMessage message, Server.ClientThread userWhoMadeMove) {
        if (userWhoMadeMove != currentPlayer) return;
        boolean successfulMove = attemptMove(message.row, message.col);
        if (!successfulMove) return;
        totalMoves++;
        Pair<List<int[]>, String> winningPieces = findWinningPositions(message.row, message.col);
        if (winningPieces.getKey() != null) {
            // The game found a winning move, end the game here and notify both clients.
            Instant endTime = Instant.now();
            long minutes = Duration.between(startTime, endTime).toMinutes();
            Piece[][] boardCopy = snapshotBoard();
            player1.sendMessage(new BoardUpdate(boardCopy, currentPlayer == player1));
            player2.sendMessage(new BoardUpdate(boardCopy, currentPlayer == player2));
            String winnerUsername = userWhoMadeMove.getUsername().equals(player1.getUsername()) ? player1.getUsername() : player2.getUsername();
            String loserUsername = !userWhoMadeMove.getUsername().equals(player1.getUsername()) ? player1.getUsername() : player2.getUsername();
            guiLogger.accept(winnerUsername + " WON THE GAME BETWEEN THEM AND " + loserUsername);
            player1.sendMessage(new WinMessage(userWhoMadeMove.getUsername().equals(player1.getUsername()), winningPieces.getKey(), minutes, totalMoves, winningPieces.getValue()));
            player2.sendMessage(new WinMessage(userWhoMadeMove.getUsername().equals(player2.getUsername()), winningPieces.getKey(), minutes, totalMoves, winningPieces.getValue()));
            endGame();
            return;
        }
        if(isBoardFull()) {
            Instant endTime = Instant.now();
            long minutes = Duration.between(startTime, endTime).toMinutes();
            guiLogger.accept("GAME BETWEEN " + player1.getUsername() + " AND " + player2.getUsername() + " RESULTED IN A DRAW!");
            player1.sendMessage(new DrawMessage(minutes, totalMoves));
            player2.sendMessage(new DrawMessage(minutes, totalMoves));
            endGame();
            return;
        }
        currentPlayer = (currentPlayer == player1 ? player2 : player1);
        Piece[][] boardCopy = snapshotBoard();
        player1.sendMessage(new BoardUpdate(boardCopy, currentPlayer == player1));
        player2.sendMessage(new BoardUpdate(boardCopy, currentPlayer == player2));
    }


    private boolean attemptMove(int row, int col) {
        if(board[row][col] != Piece.EMPTY) {
            return false;
        } else {
            Piece attempter = currentPlayer == player1 ? Piece.PLAYER1 : Piece.PLAYER2;
            board[row][col] = attempter;
            return true;
        }
    }

    // Creates a copy of the server board. Used to send to clients after updating the board.
    private Piece[][] snapshotBoard() {
        Piece[][] copy = new Piece[6][7];
        for (int i = 0; i < 6; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }

    // Scans the board to check for wins in all possible directions (horizontal, vertical, and diagonal)
    private Pair<List<int[]>, String> findWinningPositions(int row, int col) {
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

    // Checks to see if there are any empty pieces in the board.
    private boolean isBoardFull() {
        for (Piece[] row : board) {
            for (Piece p : row) {
                if (p == Piece.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Scan out from (row,col) in (dr1,dc1) and (dr2,dc2), collect
     * all contiguous pieces == me (including the origin), and if
     * size>=4 return that list, else return null.
     */
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

    public void endGame() {
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    public void run() {
        gameThread = Thread.currentThread();
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
