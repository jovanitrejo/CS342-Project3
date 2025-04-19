
import MessageClasses.*;
import Scenes.GameScreen;
import Scenes.LoginScreen;
import Scenes.MainMenu;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class GuiClient extends Application{
	StackPane root = new StackPane();
	Pane currentGUI = new Pane();
	Client clientConnection;
	LoginScreen loginScreen = new LoginScreen(username -> clientConnection.send(new LoginMessage(username)));
	MainMenu mainMenu = new MainMenu(findNewGame -> clientConnection.send(new NewGameMessage()), changeUsername -> Platform.runLater(() -> {
        root.getChildren().remove(currentGUI);
        currentGUI = loginScreen.getRoot();
        root.getChildren().add(currentGUI);
    }));
	GameScreen gameScreen = new GameScreen(((row, col) -> clientConnection.send(new MoveMessage(row, col))));

	MessageDispatcher messageDispatcher = new MessageDispatcher();
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		// Initialize background image, fonts, and styles
		Font.loadFont(getClass().getResourceAsStream("/fonts/Londrina_Solid/LondrinaSolid-Regular.ttf"), 18);
		ImageView imageBackground = new ImageView(new Image("Game_Background.png"));
		imageBackground.setFitWidth(1280);
		imageBackground.setFitHeight(720);
		imageBackground.setPreserveRatio(true);

		currentGUI = loginScreen.getRoot();

		// Add image and context to root
		root.getChildren().addAll(imageBackground, currentGUI);
		clientConnection = new Client(messageDispatcher);

		// Creating handler for login response
		messageDispatcher.registerHandler(LoginResponse.class, (LoginResponse message) -> {
			if (!message.getWasSuccessful()) {
				Platform.runLater(() -> loginScreen.setWarningLabel(message.getDescription()));
			} else {
				System.out.print("Logged in!");
				Platform.runLater(() -> {
					// Move user to the main menu on successful login
					root.getChildren().remove(currentGUI);
					currentGUI = mainMenu.getRoot();
					root.getChildren().add(currentGUI);
				});
			}
		});

		// Creating handler for new game response
		messageDispatcher.registerHandler(NewGameResponse.class, (NewGameResponse message) -> Platform.runLater(() -> {
            if (!message.isInQueue()) {
                gameScreen.startNewGame(message.getOpponentUsername(), message.amIRed(), message.isItMyTurn());
            }
            root.getChildren().remove(currentGUI);
            currentGUI = gameScreen.getCurrentDisplay();
            root.getChildren().add(currentGUI);
        }));

		// Creating handler for updating gameBoards;
		messageDispatcher.registerHandler(BoardUpdate.class, (board) -> {
			System.out.println("Someone made a move! Updating the board...");
			gameScreen.state.updateBoard(board.getGameBoard());
			System.out.println("Setting your turn...");
			gameScreen.state.setIsYourTurn(board.isItMyTurn());
			System.out.println("Refreshing the UI...");
			Platform.runLater(() -> gameScreen.refreshBoardUI());
		});

		// Creating handler for handling a winner
		messageDispatcher.registerHandler(WinMessage.class, (winMessage -> {
			gameScreen.state.setIsYourTurn(false);
			gameScreen.highlightWinningPieces(winMessage.getWinningPieces());
		}));

		// Creating handler for handling a quitter
		messageDispatcher.registerHandler(QuitMessage.class, (quitMessage -> {
			System.out.println("Got a quit message!");
			Platform.runLater(() -> {
				root.getChildren().remove(currentGUI);
				gameScreen.handleQuit();
				currentGUI = mainMenu.getRoot();
				root.getChildren().add(currentGUI);
			});
		}));

							
		clientConnection.start();

		primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

		Scene scene = new Scene(root, 1280, 720);
		System.out.println(getClass().getResource("/fonts/Londrina_Solid/LondrinaSolid-Regular.ttf"));
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
		imageBackground.fitWidthProperty().bind(scene.widthProperty());
		imageBackground.fitHeightProperty().bind(scene.heightProperty());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}

}
