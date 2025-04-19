package utils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    public static Button createStyledButton(int width, int height, String backgroundColorHex, Color textColor, String buttonText, int fontSize) {
        Button customButton = new Button(buttonText);
        customButton.setMaxWidth(width);
        customButton.setMaxHeight(height);
        customButton.setTextFill(textColor);

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

}
