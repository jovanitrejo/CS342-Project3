package Scenes;

import MessageClasses.Piece;
import contexts.GameState;
import contexts.LocalGameSession;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Button;
import javafx.util.Pair;
import utils.CustomJavaFXElementsTools;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameScreen {
    private StackPane currentDisplay;
    public GameState state;
    public ChatBox chatBox;
    private final boolean isLocalPlay;
    private final BoardSpot[][] board = new BoardSpot[6][7];
    private final BiConsumer<Integer, Integer> callback;
    private final Runnable mainMenuCallback;
    private final Runnable replayButtonCallback;
    private final Runnable replayAcceptedCallback;
    private final Runnable replayDeniedCallback;
    private final Consumer<String> sendMessageCallback;
    private LocalGameSession localGameSession;
    private Text turnLabel;
    private Button makeMove;
    public Button replayButton;
    private final VBox quitPopUp;
    private int selectedRow = -1;
    private int selectedCol = -1;
    public Button quitButton;
    public String opponentUsername;
    private VBox replayPopUp;
    /** each spot knows how to paint itself from a Piece */
    private static class BoardSpot {
        private final StackPane cell = new StackPane();
        private final Text filled = new Text("");
        private final Circle circle = new Circle(30);
        InnerShadow innerShadow = new InnerShadow();

        BoardSpot() {
            cell.getChildren().addAll(filled, circle);
            cell.setPrefSize(95, 85);
            cell.setStyle("-fx-border-style: none ; -fx-background-color: #4987E9;");
            circle.setFill(Color.rgb(82, 121, 203));
            circle.setEffect(innerShadow);
            circle.setId("circle");
            innerShadow.setOffsetX(0);
            innerShadow.setOffsetY(5);
            innerShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        }

        public StackPane getCellPane() {
            return cell;
        }

        public void showSelectable() {
            cell.lookup("#circle").setStyle("-fx-stroke: #FFFFFF; -fx-stroke-width: 2px");
        }
        public void removeSelectable() {
            cell.lookup("#circle").setStyle("");
        }

        public void selectPiece() { cell.lookup("#circle").setStyle("-fx-stroke: #FFFFFF; -fx-stroke-width: 4px");}
        public void unselectPiece() { cell.lookup("#circle").setStyle("");}

        public void highlight() {
            filled.setText("W");
            cell.lookup("#circle").setStyle("-fx-stroke: #00FF66; -fx-stroke-width: 5px");
        }

        /** paint an EMPTY/PLAYER1/PLAYER2 into this cell */
        public void setColor(Color c) {
            circle.setFill(c);
        }
    }

    public GameScreen(BiConsumer<Integer, Integer> callback, Runnable mainMenuCallback, Runnable replayButtonCallback, Runnable replayAcceptedCallback, Runnable replayDeniedCallback, Consumer<String> sendMessageCallback, boolean isLocalPlay) {
        this.callback = callback;
        this.mainMenuCallback = mainMenuCallback;
        this.replayButtonCallback = replayButtonCallback;
        this.replayAcceptedCallback = replayAcceptedCallback;
        this.replayDeniedCallback = replayDeniedCallback;
        this.sendMessageCallback = sendMessageCallback;
        this.isLocalPlay = isLocalPlay;

        Text waiting = new Text("Waiting for opponent");
        waiting.setFont(Font.font("Londrina Solid", 60));
        waiting.setFill(Color.WHITE);
        waiting.setStroke(Color.BLACK);
        waiting.setStrokeWidth(1);

        Text ellipses = new Text("...");
        ellipses.setFont(Font.font("Londrina Solid", 60));
        ellipses.setFill(Color.WHITE);
        ellipses.setStroke(Color.BLACK);
        ellipses.setStrokeWidth(1);

        Button backToMainMenuButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Back to Main Menu", 24, false);
        backToMainMenuButton.setOnAction(e -> mainMenuCallback.run());

        VBox fullMessage = new VBox(waiting, ellipses); // 10 px spacing between texts
        fullMessage.setMaxWidth(650);
        fullMessage.setAlignment(Pos.CENTER);

        currentDisplay = new StackPane(fullMessage, backToMainMenuButton);
        StackPane.setAlignment(backToMainMenuButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backToMainMenuButton, new Insets(0, 0, 50, 50));
        StackPane.setAlignment(fullMessage, Pos.CENTER);
        quitButton = CustomJavaFXElementsTools.createStyledButton(100, 50, "#FF0000", Color.WHITE, "Quit", 24, true);
        quitButton.setOnAction(e -> {showPopUp(); hideQuitButton();});
        quitPopUp = CustomJavaFXElementsTools.createPopUp(() -> {mainMenuCallback.run(); handleQuit();}, () -> {hidePopUp(); showQuitButton();}, "Are you sure you want to quit?", "Yes", "No");
    }

    public void startNewGame(String opponent, boolean isRed, boolean isYourTurn, int playerSlot) {
        state = new GameState(isRed, isYourTurn, playerSlot);

        if (isLocalPlay) {
            localGameSession = new LocalGameSession();
        }

        System.out.println("You are " + (isRed ? "red!" : "yellow!"));
        System.out.println("You are playing against: " + opponent);
        // Adding opponent info to screen
        VBox opponentInfo;
        Text filler = new Text("Current Opponent:");
        filler.setFont(Font.font("Londrina Solid", 24));
        filler.setFill(Color.WHITE);
        filler.setStroke(Color.BLACK);
        filler.setStrokeWidth(1); // Thin black stroke
        filler.setTextAlignment(TextAlignment.CENTER);

        // Initialize chat
        chatBox = new ChatBox(state.getMyColor(), state.getOppColor(), sendMessageCallback);

        this.opponentUsername = opponent;
        Text opponentName = new Text(opponent);
        opponentName.setFont(Font.font("Londrina Solid", 36));
        opponentName.setFill(isRed
                ? Color.web("#FFE123")   // if you are red, opponent is yellow
                : Color.web("#FF0000")); // If you are yellow, opponent is red
        opponentName.setStroke(Color.BLACK);
        opponentName.setStrokeWidth(1); // Thin black stroke
        opponentName.setTextAlignment(TextAlignment.CENTER);

        opponentInfo = new VBox(filler, opponentName);
        opponentInfo.setPrefSize(300, 120);
        opponentInfo.setPickOnBounds(false);

        // Adding text to inform whose turn it is
        String initialTurnLabelString;
        if (isLocalPlay) {
            initialTurnLabelString = "Player 1's turn...";
        } else {
            initialTurnLabelString = isYourTurn ? "Your turn..." : "Opponent's Turn...";
        }
        turnLabel = new Text(initialTurnLabelString);
        turnLabel.setFont(Font.font("Londrina Solid", 36));
        turnLabel.setFill(Color.WHITE);
        turnLabel.setStroke(Color.BLACK);
        turnLabel.setPickOnBounds(false);

        // Initialize the make move button.
        makeMove = CustomJavaFXElementsTools.createStyledButton(150, 50, isRed ? "#FF0000" : "#FFE123", Color.WHITE, "Make Move", 24, true);
        makeMove.setOnAction(e -> {
            if (selectedRow == -1 || selectedCol == -1) return;
            board[selectedRow][selectedCol].unselectPiece();

            if (isLocalPlay) {
                // Local two-player move handling
                localGameSession.onMove(selectedRow, selectedCol); // attempt the move and update local session

                // Copy the localGameSession's board into GameState
                Piece[][] newBoard = localGameSession.getBoard();
                state.updateBoard(newBoard);

                refreshBoardUI(); // update board

                Pair<List<int[]>, String> winInfo = localGameSession.findWinningPositions(selectedRow, selectedCol);
                if (winInfo.getKey() != null) {
                    long minutes = 0;
                    state.setIsYourTurn(false);
                    this.highlightWinningPieces(winInfo.getKey());
                    showGameEnded(false, localGameSession.player1Turn, minutes, localGameSession.getTotalMoves(), winInfo.getValue());
                } else if (localGameSession.isBoardFull()) {
                    long minutes = 0;
                    state.setIsYourTurn(false);
                    showGameEnded(true, false, minutes, localGameSession.getTotalMoves(), "None");
                } else {
                    changeTurnText();
                }
            } else {
                // Server play logic (already correct)
                state.applyMove(selectedCol);
                callback.accept(selectedRow, selectedCol);
                state.setIsYourTurn(false);
                changeTurnText();
            }
            selectedRow = -1;
            selectedCol = -1;
            hideMakeMoveButton();
        });
        makeMove.setPickOnBounds(false);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(30,30,30,30));

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
            for (int r = 0; r < 6; r++) {
                int finalR = r;
                board[r][c].getCellPane().setOnMouseEntered(e -> {
                    if (finalR == selectedRow && col == selectedCol) return;
                    if (!state.isYourTurn()) return;
                    int row = state.findFreeRow(col);
                    if (row == selectedRow && col == selectedCol) return;
                    board[row][col].showSelectable();
                });
                board[r][c].getCellPane().setOnMouseExited(e -> {
                    if (finalR == selectedRow && col == selectedCol) return;
                    if (!state.isYourTurn()) return;
                    int row = state.findFreeRow(col);
                    if (row == selectedRow && col == selectedCol) return;
                    board[row][col].removeSelectable();
                });
                board[r][c].getCellPane().setOnMouseClicked(e -> {
                    if (selectedRow != -1 && selectedCol != -1) {
                        board[selectedRow][selectedCol].unselectPiece();
                    }
                    if (!state.isYourTurn()) return; // ignore if not your turn
                    System.out.println("Sending move to the server!");
                    int row = state.findFreeRow(col);// model: drop the piece
                    board[row][col].removeSelectable();
                    board[row][col].selectPiece();
                    selectedRow = row;
                    selectedCol = col;
                    showMakeMoveButton();
                });
            }
        }
        currentDisplay = new StackPane(grid, opponentInfo, turnLabel, quitButton);
        if (!isLocalPlay) {
            currentDisplay.getChildren().add(chatBox.getRoot());
        }
        StackPane.setAlignment(quitButton, Pos.TOP_RIGHT);
        StackPane.setAlignment(opponentInfo, Pos.CENTER_LEFT);
        StackPane.setAlignment(turnLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(makeMove, Pos.CENTER_RIGHT);
        StackPane.setMargin(makeMove, new Insets(0, 100, 0, 0));
        StackPane.setAlignment(chatBox.getRoot(), Pos.BOTTOM_RIGHT);
        StackPane.setMargin(quitButton, new Insets(30,30,0,0));
        StackPane.setMargin(turnLabel, new Insets(30,0,0,0));
        StackPane.setMargin(opponentInfo, new Insets(325,0,0,25));
        StackPane.setMargin(chatBox.getRoot(), new Insets(0, 35, 0, 0));
    }

    // Re-renders the UI with the new game board. Will be called on a new BoardMessage.
    public void refreshBoardUI() {
        Piece[][] b = state.getGameBoard();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                Piece p = b[r][c];
                if (p == state.getMyPiece())  board[r][c].setColor(state.getMyColor());
                else if (p == state.getOppPiece()) board[r][c].setColor(state.getOppColor());
                else board[r][c].setColor(Color.rgb(82,121,203));
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

    public void showGameEnded(boolean wasDraw, boolean youWon, long lengthInMinutes, int totalNumMoves, String winDirection) {
        // hide the quit button
        hideQuitButton();
        if (wasDraw) {
            turnLabel.setText("IT'S A DRAW!!!");
        } else {
            if (youWon) {
                if (isLocalPlay) {
                    turnLabel.setText("Player 1 Wins!!!");
                    turnLabel.setFill(Color.web("#00D928"));
                } else {
                    turnLabel.setText("YOU WIN!!!");
                    turnLabel.setFill(Color.web("#00D928"));
                }
            } else {
                if (isLocalPlay) {
                    turnLabel.setText("Player 2 Wins!!!");
                    turnLabel.setFill(Color.web("#00D928"));
                } else {
                    turnLabel.setText("YOU LOST!!!");
                    turnLabel.setFill(Color.web("#FF0000"));
                }
            }
        }
        // Construct a replay option menu
        replayButton = CustomJavaFXElementsTools.createStyledButton(215, 50, "#26940B", Color.WHITE, "Replay", 24, false);
        Button quitButton = CustomJavaFXElementsTools.createStyledButton(215, 50, "#26940B", Color.WHITE, "Back to Main Menu", 24, false);
        quitButton.setOnAction(e -> mainMenuCallback.run());
        replayButton.setOnAction(e -> {
            replayButtonCallback.run();
            replayButton.setText("Sent!");
            replayButton.setDisable(true);
        });
        Text gameStats = new Text(state.amIRed() ? (youWon ? " RED WINS!" : " YELLOW WINS!") : (youWon ? " YELLOW WINS!" : " RED WINS!"));
        Text statusLabel = new Text(" Game Stats:");
        Text winType = !Objects.equals(winDirection, " None") ? new Text(" " + winDirection +" Win") : new Text(winDirection);
        Text totalMoves = new Text(" Total Moves: " + totalNumMoves);
        Text time = new Text(" Elapsed: " + lengthInMinutes + " min");
        gameStats.setFont(new Font("Londrina Solid", 36));
        gameStats.setFill(Color.BLACK);
        statusLabel.setFont(new Font("Londrina Solid", 36));
        statusLabel.setFill(Color.BLACK);
        winType.setFont(new Font("Londrina Solid", 36));
        winType.setFill(Color.BLACK);
        totalMoves.setFont(new Font("Londrina Solid", 36));
        totalMoves.setFill(Color.BLACK);
        time.setFont(new Font("Londrina Solid", 36));
        time.setFill(Color.BLACK);

        VBox buttons = new VBox(20, replayButton, quitButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(30,0,0,0));

        VBox optionsMenu = new VBox(gameStats, statusLabel, winType, totalMoves, time, buttons);
        optionsMenu.setPrefSize(260, 400);
        optionsMenu.setMaxSize(260, 400);
        optionsMenu.setStyle("-fx-background-color: #FBFDD6");
        StackPane.setAlignment(gameStats, Pos.TOP_CENTER);
        StackPane.setAlignment(buttons, Pos.BOTTOM_CENTER);

        // Add to screen
        Group menuGroup = new Group(optionsMenu);
        StackPane.setAlignment(menuGroup, Pos.TOP_RIGHT);
        StackPane.setMargin(menuGroup, new Insets(105,30,0,0));
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        if (!isLocalPlay) {
            int chatIndex = kids.indexOf(chatBox.getRoot()); // add before chat box
            kids.add(chatIndex, menuGroup);
        } else {
            kids.add(menuGroup);
        }
    }

    public void showReplayPopUp() {
        replayPopUp = CustomJavaFXElementsTools.createPopUp(
                () -> {
                    replayDeniedCallback.run();
                    hideReplayPopUp();
                },
                replayAcceptedCallback,
                this.opponentUsername + " has requested to rematch you! Do you accept?",
                "No",
                "Yes");
        currentDisplay.getChildren().add(replayPopUp);
    }

    private void hideReplayPopUp() {
        ObservableList<Node> kids = currentDisplay.getChildren();
        kids.remove(replayPopUp);
        this.replayPopUp = null;
    }

    public StackPane getCurrentDisplay() {
        return currentDisplay;
    }

    public void changeTurnText() {
        if (isLocalPlay) {
            turnLabel.setText(localGameSession.player1Turn ? "Player 1's Turn..." : "Player 2's Turn...");
        } else {
            turnLabel.setText(state.isYourTurn() ? "Your Turn..." : "Opponent's Turn...");
        }
    }

    public void handleQuit() {
        Text waiting = new Text("Waiting for opponent");
        waiting.setFont(Font.font("Londrina Solid", 60));
        waiting.setFill(Color.WHITE);
        waiting.setStroke(Color.BLACK);
        waiting.setStrokeWidth(1);

        Text ellipses = new Text("...");
        ellipses.setFont(Font.font("Londrina Solid", 60));
        ellipses.setFill(Color.WHITE);
        ellipses.setStroke(Color.BLACK);
        ellipses.setStrokeWidth(1);

        VBox fullMessage = new VBox(waiting, ellipses); // 10 px spacing between texts
        fullMessage.setMaxWidth(650);
        fullMessage.setAlignment(Pos.CENTER);
        currentDisplay = new StackPane(fullMessage);
        StackPane.setAlignment(waiting, Pos.CENTER);
    }

    public void showMakeMoveButton() {
        if (selectedCol == -1 || selectedRow == -1) return;
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        if (!kids.contains(makeMove)) {
            if (isLocalPlay) {
                String backgroundColorHex = localGameSession.player1Turn ? "FF0000" : "FFE123";
                String hoverColor = CustomJavaFXElementsTools.darkenHexColor(backgroundColorHex, 0.8); // 80% brightness

                String baseStyle = "-fx-font-size: " + 24 + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-background-color: " + backgroundColorHex + ";";

                String hoverStyle = baseStyle.replace(backgroundColorHex, hoverColor);

                makeMove.setStyle(baseStyle);
                makeMove.setOnMouseEntered(e -> makeMove.setStyle(hoverStyle));
                makeMove.setOnMouseExited(e -> makeMove.setStyle(baseStyle));
                kids.add(makeMove);
            } else {
                // find where the chat box lives:
                int chatIndex = kids.indexOf(chatBox.getRoot());
                // insert the button just before it:
                kids.add(chatIndex, makeMove);
            }
        }
    }

    public void hideMakeMoveButton() {
        if (selectedCol != -1 || selectedRow != -1) return;
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        kids.remove(makeMove);
    }

    private void hidePopUp() {
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        kids.remove(quitPopUp);
    }

    private void showPopUp() {
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        if (!kids.contains(quitPopUp)) {
            kids.add(quitPopUp);
        }
    }

    private void hideQuitButton() {
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        kids.remove(quitButton);
    }

    private void showQuitButton() {
        ObservableList<Node> kids = getCurrentDisplay().getChildren();
        if (!kids.contains(quitButton)) {
            kids.add(quitButton);
        }
    }
}