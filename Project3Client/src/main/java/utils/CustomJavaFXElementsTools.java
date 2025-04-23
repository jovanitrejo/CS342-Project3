package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class CustomJavaFXElementsTools {
    public static VBox createCustomLabel(String textDisplay) {
        Text text = new Text(textDisplay);
        text.setFont(Font.font("Londrina Solid", 60));
        text.setFill(Color.WHITE);
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(1); // Thin black stroke
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(430);

        Rectangle background = new Rectangle(450, 100);
        background.setFill(Color.web("#9E9E9E", 0.5));
        background.setArcWidth(15);
        background.setArcHeight(15);

        StackPane labelPane = new StackPane(background, text);
        labelPane.setPrefSize(450, 100);
        StackPane.setAlignment(text, Pos.CENTER);

        VBox wrapped = new VBox(labelPane);
        wrapped.setAlignment(Pos.TOP_CENTER);

        return wrapped;
    }

    public static TextField createCustomTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setStyle("-fx-font-size: 60; -fx-alignment: center; -fx-border-color: black; -fx-border-width: 1; -fx-background-radius: 15; -fx-border-radius: 15");
        tf.setMaxWidth(650);
        tf.setMaxHeight(100);
        return tf;
    }

    public static Button createStyledButton(int width, int height, String backgroundColorHex, Color textColor, String buttonText, int fontSize, boolean addStroke) {
        Button customButton = new Button(buttonText);
        customButton.setMaxWidth(width);
        customButton.setMaxHeight(height);
        customButton.setTextFill(textColor);

        if (addStroke) {
            // Stroke effect on the text.
            Text txt = new Text(buttonText);
            txt.setFont(Font.font(fontSize));
            txt.setFill(textColor);           // interior color
            txt.setStroke(Color.BLACK);       // outline color
            txt.setStrokeWidth(1);            // outline thickness

            // tell the Button to show *only* our graphic
            customButton.setGraphic(txt);
            customButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        String hoverColor = darkenHexColor(backgroundColorHex, 0.8); // 80% brightness

        String baseStyle = "-fx-font-size: " + fontSize + ";" +
                "-fx-border-color: black;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;" +
                "-fx-background-color: " + backgroundColorHex + ";";

        String hoverStyle = baseStyle.replace(backgroundColorHex, hoverColor);

        customButton.setStyle(baseStyle);

        // Set hover behavior
        customButton.setOnMouseEntered(e -> customButton.setStyle(hoverStyle));
        customButton.setOnMouseExited(e -> customButton.setStyle(baseStyle));

        return customButton;
    }

    public static String darkenHexColor(String hexColor, double factor) {
        // Remove the '#' and parse each color component
        hexColor = hexColor.replace("#", "");
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        // Apply the darkening factor
        r = (int)(r * factor);
        g = (int)(g * factor);
        b = (int)(b * factor);

        // Clamp values and return new hex string
        return String.format("#%02X%02X%02X", clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static VBox createPopUp(Runnable button1Callback, Runnable button2Callback, String popupText, String button1Text, String button2Text) {
        Text message = new Text(popupText);
        message.setFont(Font.font("Londrina Solid", 60));
        message.setStroke(Color.BLACK);
        message.setStrokeWidth(1);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFill(Color.WHITE);
        message.setWrappingWidth(590);

        Button button1 = createStyledButton(175, 50, "#FF0000", Color.WHITE, button1Text, 24, true);
        button1.setOnAction(e -> button1Callback.run());
        Button button2 = createStyledButton(175, 50, "#1DFA00", Color.WHITE, button2Text, 24, true);
        button2.setOnAction(e -> button2Callback.run());

        HBox buttonOptions = new HBox(50, button1, button2);
        VBox popUp = new VBox(message, buttonOptions);

        //buttons positioning and size
        button1.setTextAlignment(TextAlignment.CENTER);
        button2.setTextAlignment(TextAlignment.CENTER);
        button1.setPrefSize(175,50);
        button2.setPrefSize(175,50);

        //popUp positioning and size
        popUp.setSpacing(55);
        popUp.setAlignment(Pos.CENTER);
        buttonOptions.setAlignment(Pos.CENTER);
        popUp.setMaxSize(600, 400);
        popUp.setPrefSize(600, 400);
        popUp.setBackground(new Background(
                new BackgroundFill(
                        // semi‑transparent gray (75% opacity)
                        Color.web("#7D7D7D", 0.75),
                        // 10 px corner radius on all corners
                        new CornerRadii(15),
                        // no additional insets
                        Insets.EMPTY
                )
        ));
        popUp.setStyle("-fx-border-color: black; -fx-border-radius: 15");

        return popUp;
    }

}
