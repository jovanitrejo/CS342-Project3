package Scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import utils.CustomJavaFXElementsTools;

import java.util.function.Consumer;

public class LoginScreen {
    StackPane root;
    public TextField userNameTextField = CustomJavaFXElementsTools.createCustomTextField("Enter a username");
    Label warningLabel = new Label();
    Consumer<String> callback;
    public LoginScreen(Consumer<String> callback) {
        this.callback = callback;

        userNameTextField.setOnAction(e -> {
            System.out.println("Trying to send new username to server...");
            callback.accept(userNameTextField.getText());
            System.out.println("Your username is set to: " + userNameTextField.getText());
        });

        // Title Label
        VBox topLabel = CustomJavaFXElementsTools.createCustomLabel("CS342 Connect 4");
        topLabel.setAlignment(Pos.TOP_CENTER);
        topLabel.setPadding(new Insets(160, 0, 0, 0));

        // Center input box
        VBox loginBox = new VBox(10, warningLabel, userNameTextField);
        loginBox.setAlignment(Pos.CENTER);
        warningLabel.setTextFill(Color.RED);
        warningLabel.setStyle("-fx-font-size: 24");

        // Root container
        root = new StackPane(topLabel, loginBox);
        StackPane.setAlignment(topLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(loginBox, Pos.CENTER);
    }

    public StackPane getRoot() {
        return this.root;
    }

    public void setWarningLabel(String text) {
        Platform.runLater(() -> warningLabel.setText(text));
    }
}
