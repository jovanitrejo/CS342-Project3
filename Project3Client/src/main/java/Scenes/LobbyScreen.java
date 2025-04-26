package Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import utils.CustomJavaFXElementsTools;

import java.util.HashMap;
import java.util.Stack;
import java.util.function.Consumer;

public class LobbyScreen {
    VBox screen = new VBox();
    public boolean inLobby = false;
    Text waitingText = new Text("Waiting for someone to join you...");
    Button backToMainMenuButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Back to Main Menu", 24, false);
    Button joinLobbyButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Join Lobby", 24, false);
    private final Consumer<String> joinUserCallback;
    StackPane root;

    HashMap<String, HBox> usersInLobby = new HashMap<>();

    public LobbyScreen(Runnable backToMainMenuCallback, Consumer<String> joinUserCallback, Runnable joinLobbyCallback, Runnable leaveLobbyCallback) {

        waitingText.setFont(Font.font("Londrina Solid", 60));
        waitingText.setFill(Color.WHITE);
        waitingText.setStroke(Color.BLACK);
        waitingText.setStrokeWidth(1);
        waitingText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(waitingText, Pos.CENTER);

        this.joinUserCallback = joinUserCallback;
        backToMainMenuButton.setOnAction(e -> backToMainMenuCallback.run());
        screen.setPrefSize(600, 400);
        screen.setMaxSize(600, 400);
        screen.setSpacing(20);
        screen.setBackground(new Background(new BackgroundFill(
                Color.rgb(125, 125, 125, 0.75),
                new CornerRadii(15),
                Insets.EMPTY
        )));
        joinLobbyButton.setOnAction(e -> {
            if (!inLobby) {
                inLobby = true;
                joinLobbyButton.setText("Leave Lobby");
                // Hide the list of users
                root.getChildren().remove(screen);
                root.getChildren().add(waitingText);
                joinLobbyCallback.run();
            } else {
                inLobby = false;
                joinLobbyButton.setText("Join Lobby");
                // Show the list of users in the lobby
                root.getChildren().remove(waitingText);
                root.getChildren().add(screen);
                leaveLobbyCallback.run();
            }
        });
        backToMainMenuButton.setOnAction(e -> {
            if (inLobby) {
                joinLobbyButton.fire();
            }
            backToMainMenuCallback.run();
        });

        root = new StackPane(backToMainMenuButton, joinLobbyButton, screen);
        StackPane.setAlignment(screen, Pos.CENTER);
        StackPane.setAlignment(backToMainMenuButton, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(joinLobbyButton, Pos.BOTTOM_RIGHT);

        root.setPickOnBounds(false);
        backToMainMenuButton.setPickOnBounds(false);
        joinLobbyButton.setPickOnBounds(false);

        //styles
        Text lobbyTitle = new Text("Available Players");
        lobbyTitle.setFill(Color.WHITE);
        lobbyTitle.setStroke(Color.BLACK);
        lobbyTitle.setStrokeWidth(1);
        lobbyTitle.setStyle("-fx-font-size: 40px");

        HBox lobbyBox = new HBox(lobbyTitle);
        screen.getChildren().add(lobbyBox);
        lobbyBox.setAlignment(Pos.CENTER);
        StackPane.setMargin(backToMainMenuButton, new Insets(0, 0, 50, 50));
        StackPane.setMargin(joinLobbyButton, new Insets(0, 50, 50, 0));
        backToMainMenuButton.setPrefWidth(200);
        backToMainMenuButton.setPrefHeight(50);
        joinLobbyButton.setPrefWidth(200);
        joinLobbyButton.setPrefHeight(50);

    }

    public void addToLobbyScreen(String user) {
        Text username = new Text(user);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button joinButton = CustomJavaFXElementsTools.createStyledButton(100, 50, "#00B2FF", Color.WHITE, "Join Game", 24, true);
        HBox container = new HBox(username, spacer, joinButton);
        container.setPadding(new Insets(0, 20, 0, 20));
        joinButton.setOnMouseClicked(e -> joinUserCallback.accept(user));
        usersInLobby.put(user, container);

        // Add to screen
        screen.getChildren().add(container);
        container.setAlignment(Pos.CENTER);
        username.setFill(Color.WHITE);
        username.setStyle("-fx-font-size: 30px;");
        username.setStroke(Color.BLACK);
        username.setStrokeWidth(1);
    }

    public void removeFromLobbyScreen(String user) {
        if (usersInLobby.containsKey(user)) {
            // Remove from screen
            screen.getChildren().remove(usersInLobby.get(user));
        }
    }

    public void leaveLobby() {
        joinLobbyButton.fire();
    }

    public StackPane getRoot() {
        return this.root;
    }
}
