package Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import utils.CustomJavaFXElementsTools;

import java.util.HashMap;
import java.util.function.Consumer;

public class LobbyScreen {
    VBox screen = new VBox();
    public boolean inLobby = false;
    Button backToMainMenuButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Back to Main Menu", 24, false);
    Button joinLobbyButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Join Lobby", 24, false);
    private final Consumer<String> joinUserCallback;
    StackPane root;

    HashMap<String, HBox> usersInLobby = new HashMap<>();

    public LobbyScreen(Runnable backToMainMenuCallback, Consumer<String> joinUserCallback, Runnable joinLobbyCallback, Runnable leaveLobbyCallback) {
        this.joinUserCallback = joinUserCallback;
        backToMainMenuButton.setOnAction(e -> backToMainMenuCallback.run());
        screen.setPrefSize(800, 600);
        screen.setMaxSize(800, 600);
        screen.setSpacing(20);
        screen.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                new CornerRadii(15),
                Insets.EMPTY
        )));
        joinLobbyButton.setOnAction(e -> {
            if (!inLobby) {
                inLobby = true;
                joinLobbyButton.setText("Leave Lobby");
                // Hide the list of users
                root.getChildren().remove(screen);
                joinLobbyCallback.run();
            } else {
                inLobby = false;
                joinLobbyButton.setText("Join Lobby");
                // Show the list of users in the lobby
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
    }

    public void addToLobbyScreen(String user) {
        Text username = new Text(user);
        HBox container = new HBox(username);
        container.setOnMouseClicked(e -> joinUserCallback.accept(user));
        usersInLobby.put(user, container);

        // Add to screen
        screen.getChildren().add(container);
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
