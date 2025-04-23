package Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import utils.CustomJavaFXElementsTools;

import java.util.function.Consumer;

public class MainMenu {
    private final StackPane root;
    public final VBox onlineUsersList;

    public MainMenu(Consumer<Boolean> findNewGameCallback, Consumer<Boolean> changeUsernameCallback) {
        final VBox welcomeText = CustomJavaFXElementsTools.createCustomLabel("CS342 Connect 4");

        welcomeText.setAlignment(Pos.TOP_CENTER);
        welcomeText.setPadding(new Insets(65, 0, 0, 0));

        Button findNewGameButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Find New Game", 24, false);
        findNewGameButton.setOnAction(e -> findNewGameCallback.accept(true));

        Button changeUsernameButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#656565", Color.WHITE, "Change Username", 24, false);
        changeUsernameButton.setOnAction(e -> changeUsernameCallback.accept(true));

        VBox options = new VBox(20, findNewGameButton, changeUsernameButton);
        options.setAlignment(Pos.CENTER);

        // List of online users displayed on the center-right of the screen
        onlineUsersList = new VBox();
        onlineUsersList.setSpacing(20);
        Text onlineUsersText = new Text("Active Users");
        onlineUsersText.setFont(Font.font("Londrina Solid", 32));
        onlineUsersText.setStroke(Color.BLACK);
        onlineUsersText.setStrokeWidth(1);
        ScrollPane scrollableList = new ScrollPane(onlineUsersList);
        VBox fullWindow = new VBox(onlineUsersText, scrollableList);
        fullWindow.setPickOnBounds(false);
        fullWindow.setBackground(new Background(new BackgroundFill(
                Color.web("#7D7D7D", 0.75),
                new CornerRadii(15),
                Insets.EMPTY
        )));
        fullWindow.setMaxSize(275, 400);


        root = new StackPane(welcomeText, options, fullWindow);
        StackPane.setAlignment(welcomeText, Pos.TOP_CENTER);
        StackPane.setAlignment(options, Pos.CENTER);
        StackPane.setAlignment(fullWindow, Pos.CENTER_RIGHT);
    }

    public StackPane getRoot() {
        return this.root;
    }
}
