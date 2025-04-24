
import MessageClasses.*;
import Scenes.GameScreen;
import Scenes.LobbyScreen;
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
	private String username;
	StackPane root = new StackPane();
	Pane currentGUI = new Pane();
	Client clientConnection;

	LoginScreen loginScreen;
	MainMenu mainMenu;
	GameScreen gameScreen;
	LobbyScreen lobbyScreen;

	public void setupScreens() {
		loginScreen = new LoginScreen((username) -> clientConnection.send(new LoginMessage(username)));

		mainMenu = new MainMenu(
				findNewGame -> clientConnection.send(new NewGameMessage()),
				changeUsername -> Platform.runLater(() -> {
					root.getChildren().remove(currentGUI);
					currentGUI = loginScreen.getRoot();
					root.getChildren().add(currentGUI);
				}),
				(opponent) -> clientConnection.send(new InviteAcceptedMessage(opponent)),
				(opponent) -> clientConnection.send(new InviteDeniedMessage(opponent)),
				(opponent) -> clientConnection.send(new InviteUserMessage(opponent)),
				() -> Platform.runLater(() -> {
					root.getChildren().remove(currentGUI);
					currentGUI = lobbyScreen.getRoot();  // lobbyScreen is now safely initialized
					root.getChildren().add(currentGUI);
				})
		);

		gameScreen = new GameScreen(
				(row, col) -> clientConnection.send(new MoveMessage(row, col)),
				() -> {
					clientConnection.send(new QuitMessage());
					Platform.runLater(() -> {
						root.getChildren().remove(currentGUI);
						currentGUI = mainMenu.getRoot();
						root.getChildren().add(currentGUI);
					});
				},
				() -> clientConnection.send(new ReplayRequest()),
				() -> clientConnection.send(new ReplayResponse(true)),
				() -> clientConnection.send(new ReplayResponse(false)),
				(message) -> clientConnection.send(new ChatMessage(message))
		);

		lobbyScreen = new LobbyScreen(
				() -> Platform.runLater(() -> {
					root.getChildren().remove(currentGUI);
					currentGUI = mainMenu.getRoot(); // safe to reference now
					root.getChildren().add(currentGUI);
				}),
				(opponentUsername) -> clientConnection.send(new InviteAcceptedMessage(opponentUsername)),
				() -> clientConnection.send(new UserLobbyUpdateMessage(true)),
				() -> clientConnection.send(new UserLobbyUpdateMessage(false))
		);
	}

	MessageDispatcher messageDispatcher = new MessageDispatcher();
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		// Initialize background image, fonts, and styles
		setupScreens();
		Font.loadFont(getClass().getResourceAsStream("/fonts/Londrina_Solid/LondrinaSolid-Regular.ttf"), 18);
		ImageView imageBackground = new ImageView(new Image("Game_Background.png"));
		imageBackground.setFitWidth(1280);
		imageBackground.setFitHeight(720);
		imageBackground.setPreserveRatio(false);

		// Initialize login screen
		loginScreen = new LoginScreen(username -> clientConnection.send(new LoginMessage(username)));

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
				username = loginScreen.userNameTextField.getText();
				Platform.runLater(() -> {
					// Move user to the main menu on successful login
					root.getChildren().remove(currentGUI);
					currentGUI = mainMenu.getRoot();
					root.getChildren().add(currentGUI);
					loginScreen.setWarningLabel("");
				});
			}
		});

		// Creating handler for new game response
		messageDispatcher.registerHandler(NewGameResponse.class, (NewGameResponse message) -> Platform.runLater(() -> {
			// leave the lobby if in it
			if (lobbyScreen.inLobby) {
				lobbyScreen.leaveLobby();
			}
			gameScreen.startNewGame(message.getOpponentUsername(), message.amIRed(), message.isItMyTurn(), message.getPlayerSlot());
			root.getChildren().remove(currentGUI);
            currentGUI = gameScreen.getCurrentDisplay();
            root.getChildren().add(currentGUI);
        }));

		//. Creating handler for in queue response
		messageDispatcher.registerHandler(WaitingInQueueMessage.class, (WaitingInQueueMessage message) -> Platform.runLater(() -> {
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
			Platform.runLater(() -> {
				gameScreen.refreshBoardUI();
				gameScreen.changeTurnText();
			});
		});

		// Creating handler for handling a winner
		messageDispatcher.registerHandler(WinMessage.class, (winMessage -> {
			gameScreen.state.setIsYourTurn(false);
			Platform.runLater(() -> {
				gameScreen.highlightWinningPieces(winMessage.getWinningPieces());
				gameScreen.showGameEnded(false, winMessage.didIWin(), winMessage.getLengthInMinutes(), winMessage.getTotalMoves(), winMessage.getWinType());
			});
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

		// Creating handler for handling a draw game
		messageDispatcher.registerHandler(DrawMessage.class, (drawMessage -> Platform.runLater(() -> gameScreen.showGameEnded(true, false, drawMessage.getLengthInMinutes(), drawMessage.getTotalMoves(), "None"))));

		// Creating handler for handling a replay request
		messageDispatcher.registerHandler(ReplayRequest.class, (replayRequest -> Platform.runLater(() -> gameScreen.showReplayPopUp())));

		// Creating handler for handling a replay response
		messageDispatcher.registerHandler(ReplayResponse.class, (replayResponse -> {
			if (!replayResponse.isAccepted()) {
				Platform.runLater(() -> {
					gameScreen.replayButton.setText("Replay");
					gameScreen.replayButton.setDisable(false);
				});
			}
		}));

		// Creating handler for updating the list of available users
		messageDispatcher.registerHandler(AvailableUsersMessage.class, (availableUsersMessage -> Platform.runLater(() -> mainMenu.updateActiveUsers(availableUsersMessage.getActiveUsers(), username))));

		// Creating a handler for getting a new chat-message in game
		messageDispatcher.registerHandler(ChatMessage.class, (chatMessage -> {
			System.out.println("Got a new chat from opponent!");
			if (gameScreen.state == null) return;
			System.out.println("Adding new chat to chat box...");
			Platform.runLater(() -> gameScreen.chatBox.addOpponentMessage(chatMessage.getMessage(), gameScreen.opponentUsername));
		}));

		// Creating a handler for getting an invitation to play a new game
		messageDispatcher.registerHandler(InviteUserMessage.class, (inviteUserMessage -> Platform.runLater(() -> mainMenu.showInvitePopup(inviteUserMessage.getUsername()))));

		// Creating a handler for when a user denies the invitation
		messageDispatcher.registerHandler(InviteDeniedMessage.class, (inviteDeniedMessage -> Platform.runLater(() -> mainMenu.reEnableInviteButton(inviteDeniedMessage.getOpponent()))));

		// Creating a handler for when a user tries to accept an invitation but the requestor joined a different one
		messageDispatcher.registerHandler(UserAlreadyInGameMessage.class, (userAlreadyInGameMessage -> Platform.runLater(() -> mainMenu.showRequestorIsAlreadyInGame())));

		// Creating a handler for when a user joins or leaves the lobby
		messageDispatcher.registerHandler(UpdateLobbyMessage.class, (updateLobbyMessage -> {
			System.out.println("The lobby has been updated!");
			Platform.runLater(() -> {
				if (updateLobbyMessage.isJoinedLobby()) {
					lobbyScreen.addToLobbyScreen(updateLobbyMessage.getUser());
				} else {
					lobbyScreen.removeFromLobbyScreen(updateLobbyMessage.getUser());
				}
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
