package Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import utils.CustomJavaFXElementsTools;

import java.util.function.Consumer;

public class MainMenu {
    private final StackPane root;

    public MainMenu(Consumer<Boolean> findNewGameCallback, Consumer<Boolean> changeUsernameCallback) {
        final VBox welcomeText = CustomJavaFXElementsTools.createCustomLabel("CS342 Connect 4");

        welcomeText.setAlignment(Pos.TOP_CENTER);
        welcomeText.setPadding(new Insets(65, 0, 0, 0));

        Button findNewGameButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Find New Game", 24);
        findNewGameButton.setOnAction(e -> findNewGameCallback.accept(true));

        Button changeUsernameButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#656565", Color.WHITE, "Change Username", 24);
        changeUsernameButton.setOnAction(e -> changeUsernameCallback.accept(true));

        VBox options = new VBox(20, findNewGameButton, changeUsernameButton);
        options.setAlignment(Pos.CENTER);

        root = new StackPane(welcomeText, options);
        StackPane.setAlignment(welcomeText, Pos.TOP_CENTER);
        StackPane.setAlignment(options, Pos.CENTER);
    }

    public StackPane getRoot() {
        return this.root;
    }
}
