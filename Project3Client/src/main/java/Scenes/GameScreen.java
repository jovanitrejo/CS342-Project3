package Scenes;

import MessageClasses.Piece;
import contexts.GameState;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.BiConsumer;

public class GameScreen {
    private StackPane currentDisplay;
    public  GameState state;
    private final BoardSpot[][] board = new BoardSpot[6][7];
    private final BiConsumer<Integer, Integer> callback;

    /** each spot knows how to paint itself from a Piece */
    private static class BoardSpot {
        private final StackPane cell = new StackPane();
        private final Text filled    = new Text("");
        private final Circle circle = new Circle(20);

        BoardSpot() {
//            cell.getChildren().add(filled);
            //edits here
            cell.getChildren().addAll(filled, circle);
            cell.setPrefSize(50, 50);
            cell.setStyle("-fx-border-style: none ; -fx-background-color: #4987E9;");
            circle.setFill(Color.rgb(82, 121, 203));
        }

        public StackPane getCellPane() {
            return cell;
        }

        public void highlight() {
            filled.setText("W");
            cell.setStyle("-fx-border-color: gray; -fx-background-color: gold;");
        }

        /** paint an EMPTY/PLAYER1/PLAYER2 into this cell */
        public void updatePiece(Piece piece) {
            switch (piece) {
//                case EMPTY:   filled.setText("");   break;
//                case PLAYER1: filled.setText("R");  break;
//                case PLAYER2: filled.setText("Y");  break;
                //edits
                case EMPTY:   circle.setFill(Color.rgb(82, 121, 203));   break;
                case PLAYER1: circle.setFill(Color.rgb(200, 25, 25));  break;
                case PLAYER2: circle.setFill(Color.rgb(200, 175, 25));  break;
            }
        }
    }

    public GameScreen(BiConsumer<Integer, Integer> callback) {
        this.callback = callback;
        Text waiting = new Text("Waiting for opponent...");
        currentDisplay = new StackPane(waiting);
        StackPane.setAlignment(waiting, Pos.CENTER);
    }

    public void startNewGame(String opponent, boolean isRed, boolean isYourTurn) {
        state = new GameState(opponent, isRed, isYourTurn);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        // 1) build the UI grid
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                board[r][c] = new BoardSpot();
                grid.add(board[r][c].getCellPane(), c, r);
            }
        }

        // 2) wire the top‐row clicks to drop a piece
        for (int c = 0; c < 7; c++) {
            final int col = c;
            board[0][c].getCellPane().setOnMouseClicked(e -> {
                if (!state.isYourTurn()) return; // ignore if not your turn
                System.out.println("Sending move to the server!");
                int row = state.applyMove(col);// model: drop the piece
                state.setIsYourTurn(false);
                callback.accept(row, col);
            });
        }

        currentDisplay = new StackPane(grid);
    }

    /** call this when you get a full new board back from the server */
    public void refreshBoardUI() {
        Piece[][] b = state.getGameBoard();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                board[r][c].updatePiece(b[r][c]);
            }
        }
    }

    public void highlightWinningPieces(List<int[]> winningPieces) {
        Platform.runLater(() -> {
            for (int[] pos : winningPieces) {
                int row = pos[0], col = pos[1];
                board[row][col].highlight();
            }
        });
    }

    public StackPane getCurrentDisplay() {
        return currentDisplay;
    }

    public void handleQuit() {
        Text waiting = new Text("Waiting for opponent...");
        currentDisplay = new StackPane(waiting);
        StackPane.setAlignment(waiting, Pos.CENTER);
    }
}