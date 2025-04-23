package Scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class ChatBox {
    private final VBox container;
    private final ScrollPane scrollPane;
    private final VBox   chatBox;
    private final HBox   inputBox;
    private final Button toggleButton;
    private final Consumer<String> sendMessageCallback;
    private final Color opponentColor;

    public ChatBox(Color myColor, Color opponentColor, Consumer<String> sendMessageCallback) {
        this.sendMessageCallback = sendMessageCallback;
        this.opponentColor = opponentColor;
        // 1) header + toggle
        Label headerLabel = new Label("Game Chat");
        toggleButton = new Button("↓");
        toggleButton.setOnAction(e -> toggleShowChat());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(headerLabel, spacer, toggleButton);
        header.setPrefHeight(40);
        header.setStyle("-fx-background-color: rgba(39, 38, 38, 0.6)");

        // 2) chat content
        chatBox = new VBox(6);
        chatBox.setPadding(new Insets(8));
        chatBox.setStyle("-fx-background-color: rgba(47,47,47,0.7)");
        scrollPane = new ScrollPane(chatBox);
        scrollPane.setPrefSize(450, 400);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatBox.heightProperty().addListener((o,oldV,newV) -> scrollPane.setVvalue(1.0));

        // 3) input
        TextField textBox = new TextField();
        Button sendButton = new Button();
        sendButton.setGraphic(new ImageView(new Image("Send_Button.png")));
        sendButton.setOnAction(e -> onSend(textBox, myColor));
        inputBox = new HBox(8, textBox, sendButton);
        inputBox.setPadding(new Insets(8));
        inputBox.setAlignment(Pos.CENTER);

        // 4) assemble
        container = new VBox(header, scrollPane, inputBox);
        container.setPrefWidth(450);
        container.setMaxWidth(450);
        container.setPrefHeight(500);
        container.setMaxHeight(500);

        //styles
        toggleButton.setStyle("-fx-background-color: #242323; -fx-border-radius: 0px; -fx-text-fill: #585757; -fx-font-size: 24px");
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setStyle("-fx-font-size: 24px;");
        headerLabel.setPadding(new Insets(8,0,0,3));
        scrollPane.setStyle("-fx-background-color: rgba(47, 47, 47, 0.7);");
        scrollPane.getStyleClass().add("scrollPane");
        scrollPane.getStylesheets().add("path/stylesheet.css");

        container.setStyle("-fx-background-color: rgba(39, 38, 38, 0.6);");
        chatBox.setStyle("-fx-background-color: transparent;");
        spacer.setStyle("-fx-background-color: rgba(47, 47, 47, 0.7);");

        textBox.setPrefWidth(335);
        textBox.setPrefHeight(52);
        textBox.setStyle("-fx-background-radius: 15px");
        sendButton.setStyle("-fx-background-color: #00AEFF; -fx-background-radius: 15px;");
    }

    public VBox getRoot() {
        return container;
    }

    private void toggleShowChat() {
        boolean shouldShow = !scrollPane.isVisible();
        // show/hide both the message area and the input area
        scrollPane.setVisible(shouldShow);
        scrollPane.setManaged(shouldShow);
        inputBox.setVisible(shouldShow);
        inputBox.setManaged(shouldShow);

        if (shouldShow) {
            container.setPrefHeight(500);
            container.setMaxHeight(500);
        } else {
            container.setPrefHeight(40);
            container.setMaxHeight(40);
        }

        // flip arrow
        toggleButton.setText(shouldShow ? "↓" : "↑");
    }

    private void onSend(TextField textBox, Color myColor) {
        String msg = textBox.getText().trim();
        if (msg.isEmpty()) return;
        sendMessageCallback.accept(msg);
        addMessage("Me", msg, myColor);
        textBox.clear();
    }

    public void addOpponentMessage(String message, String opponentUsername) {
        if (message.isEmpty()) return;
        System.out.println("Opponent said: " + message);
        addMessage(opponentUsername, message, opponentColor);
    }


    public void addMessage(String sender, String text, Color color) {
        Text name = new Text(sender + ": ");
        name.setFill(color);
        name.setStyle("-fx-font-weight:bold; -fx-font-size:18px;");
        Text body = new Text(text);
        body.setFill(Color.WHITE);
        body.setStyle("-fx-font-size:18px;");
        chatBox.getChildren().add(new HBox(4, name, body));
    }
}
