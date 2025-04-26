
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import MessageClasses.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GuiServer extends Application{

	HashMap<String, Scene> sceneMap;
	Server serverConnection;
	MessageDispatcher dispatcher = new MessageDispatcher();
	final ArrayList<Thread> activeGames = new ArrayList<>();
	ListView<String> listItems;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		serverConnection = new Server(dispatcher, data -> Platform.runLater(()-> listItems.getItems().add(data)), () -> sendToAllAuthorizedUsers(new AvailableUsersMessage(getAvailableUsers())));

		// Handle Login Requests on the server
		dispatcher.registerHandler(LoginMessage.class, (loginMessage, clientThread) -> {
			if (Objects.equals(loginMessage.getUsername(), "") || loginMessage.getUsername().contains(" ")) {
				clientThread.sendMessage(new LoginResponse(false, "Username cannot be empty or contain spaces!"));
				Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " tried setting their username to " + loginMessage.getUsername() +". but it does not meet requirements. Sending error to client."));
				return;
			}
			for(Server.ClientThread client : serverConnection.clients) {
				if (client.isAuthorized() && (client.getUsername().equals(loginMessage.getUsername()))) {
					if (client == clientThread) {
						try {
							clientThread.sendMessage(new LoginResponse(false, "You already set your username to this. Try a new one."));
							Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " tried setting their username to " + loginMessage.getUsername() +". But it's taken. Sending error to client."));
							return;
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					try {
						clientThread.sendMessage(new LoginResponse(false, "Username is already taken. Try a new one!"));
						Platform.runLater(() -> listItems.getItems().add("Client #" + clientThread.count + " tried setting a username. But it's taken. Send error to client."));
						return;
					} catch (Exception e) {
						Platform.runLater(() -> listItems.getItems().add("Error occurred when sending MessageClasses.LoginResponse to client #" + clientThread.count));
						return;
					}
				}
			}
			try {
				if (clientThread.isAuthorized()) {
					clientThread.setUsername(loginMessage.getUsername());
					clientThread.sendMessage(new LoginResponse(true, "Successfully renamed username to " + loginMessage.getUsername()));
					Platform.runLater(() -> listItems.getItems().add("Client #" + clientThread.count + " is renaming username to " + loginMessage.getUsername()));
				} else {
					clientThread.setAuthorized(true);
					clientThread.setUsername(loginMessage.getUsername());
					clientThread.sendMessage(new LoginResponse(true, "You are now authorized. Welcome " + loginMessage.getUsername() + "!"));
					Platform.runLater(() -> listItems.getItems().add("The server set client #" + clientThread.count + "'s username to " + loginMessage.getUsername() + " and is now authorized."));
				}
				// Send to user a list of people currently in the lobby
				Platform.runLater(() -> listItems.getItems().add("Notifying " + clientThread.getUsername() + " of other users who can play"));
				for (String user : serverConnection.lobby) {
					clientThread.sendMessage(new UpdateLobbyMessage(user, true));
				}

				// Notify all users of new active clients
				sendToAllAuthorizedUsers(new AvailableUsersMessage(getAvailableUsers()));
			} catch (Exception e) {
				Platform.runLater(() -> listItems.getItems().add("Error occurred when sending MessageClasses.LoginResponse to client #" + clientThread.count));
			}
		});

		// Handle NewGameRequests from clients
		dispatcher.registerHandler(NewGameMessage.class, ((newGameMessage, clientThread) -> {
			Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " wants to play a game."));
			if (!serverConnection.waitingForGame.isEmpty()) {
				Platform.runLater(() -> listItems.getItems().add("There are two people in the queue! Starting a new game..."));
				// at least one player in the queue
				Server.ClientThread opponent = serverConnection.waitingForGame.get(0);
				serverConnection.waitingForGame.remove(opponent);
				Thread newGame = getThread(opponent, clientThread);
				Platform.runLater(() -> listItems.getItems().add("Paired " + clientThread.getUsername() + " with " + opponent.getUsername() + ". Now initializing the game..."));
				synchronized(activeGames) {
					activeGames.add(newGame);
				}
				// Send clients the new list of available users since some left.
				sendToAllAuthorizedUsers(new AvailableUsersMessage(getAvailableUsers()));
				newGame.start();
			} else {
				Platform.runLater(() -> listItems.getItems().add("There are no players that are also wanting to play right now. Adding " + clientThread.getUsername() + " to queue..."));
				serverConnection.waitingForGame.add(clientThread);
				clientThread.sendMessage(new WaitingInQueueMessage());
			}
		}));

		// Handling moves
		dispatcher.registerHandler(MoveMessage.class, (moveMessage, clientThread) -> {
			Platform.runLater(() -> listItems.getItems().add("UPDATE FROM " + clientThread.activeGame.player1.getUsername() + " AND " + clientThread.activeGame.player2.getUsername() + "'S GAME: " + clientThread.getUsername() + " added a new piece to the board."));
			clientThread.activeGame.onMove(moveMessage, clientThread);
		});

		// Handling quit
		dispatcher.registerHandler(QuitMessage.class, ((quitMessage, clientThread) -> {
			if (clientThread.activeGame == null) {
				// Client left queue before could join a game, removing from queue
				Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " left the queue before someone could join them."));
                serverConnection.waitingForGame.remove(clientThread);
				return;
			}
			Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " quit the game! Shutting down active game session between " + clientThread.activeGame.player1.getUsername() + " and " + clientThread.activeGame.player2.getUsername()));
			// Remove from the list of active games
			activeGames.remove(getThread(clientThread.activeGame.player1, clientThread.activeGame.player2));
			GameSession currentGame = clientThread.activeGame.player1.activeGame;
			// Set both players' active game status to null since someone quit.
			currentGame.player1.activeGame = null;
			currentGame.player2.activeGame = null;
			// Send QuitMessage to both players
			currentGame.player1.sendMessage(new QuitMessage());
			currentGame.player2.sendMessage(new QuitMessage());
			// Send clients the new list of available users since some left.
			sendToAllAuthorizedUsers(new AvailableUsersMessage(getAvailableUsers()));
			// End game thread.
			currentGame.endGame();
		}));

		// Handling replay request
		dispatcher.registerHandler(ReplayRequest.class, ((replayRequest, clientThread) -> {
			if (clientThread.activeGame != null) {
				Server.ClientThread opponent = Objects.equals(clientThread.activeGame.player1.getUsername(), clientThread.getUsername()) ? clientThread.activeGame.player2 : clientThread.activeGame.player1;
				Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " wants to replay " + opponent.getUsername() + "... Letting them know."));
				opponent.sendMessage(new ReplayRequest(clientThread.getUsername()));
			}
		}));

		// Handling replay response
		dispatcher.registerHandler(ReplayResponse.class, ((replayResponse, clientThread) -> {
			if (clientThread.activeGame != null) {
                Server.ClientThread opponent = Objects.equals(clientThread.activeGame.player1.getUsername(), clientThread.getUsername()) ? clientThread.activeGame.player2 : clientThread.activeGame.player1;
                if (!replayResponse.isAccepted()) {
					Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " DENIED " + opponent.getUsername() + "'s request to replay"));
                    opponent.sendMessage(new ReplayResponse(replayResponse.isAccepted()));
				} else {
					// End the current game and create a new one for the two users to play.
					Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " ACCEPTED " + opponent.getUsername() + "'s request to replay"));
					GameSession currentGame = clientThread.activeGame;
					if (currentGame == null) return;
					currentGame.endGame();
					Thread newGame = getThread(opponent, clientThread);
					synchronized(activeGames) {
						activeGames.add(newGame);
					}
					newGame.start();
                }
			}
		}));

		// Handling sending a new chat message
		dispatcher.registerHandler(ChatMessage.class, ((chatMessage, clientThread) -> {
			if (clientThread.activeGame == null) return;
			Platform.runLater(() -> listItems.getItems().add("UPDATE FROM " + clientThread.activeGame.player1.getUsername() + " AND " + clientThread.activeGame.player2.getUsername() + "'S GAME: " + clientThread.getUsername() + " Sent " + chatMessage.getMessage() + " to the chat box."));
			GameSession activeGame = clientThread.activeGame;
			activeGame.chat.add(clientThread.getUsername() + ": " + chatMessage.getMessage());
			Server.ClientThread opponent = clientThread == activeGame.player1 ? activeGame.player2 : activeGame.player1;
			opponent.sendMessage(new ChatMessage(chatMessage.getMessage()));
		}));

		// Handling sending invites to users
		dispatcher.registerHandler(InviteUserMessage.class, ((inviteUserMessage, clientThread) -> {
			Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " sent an invite to " + inviteUserMessage.getUsername() + " to play a game!"));
			for (Server.ClientThread client : serverConnection.clients) {
				if (client.activeGame == null && client.isAuthorized()) {
					if (Objects.equals(client.getUsername(), inviteUserMessage.getUsername())) {
						client.sendMessage(new InviteUserMessage(clientThread.getUsername()));
					}
				}
			}
		}));

		// Handling when a user accepts an invitation
		dispatcher.registerHandler(InviteAcceptedMessage.class, (inviteAcceptedMessage, clientThread) -> {
			for (Server.ClientThread client : serverConnection.clients) {
				if (client.activeGame == null && client.isAuthorized()) {
					if (Objects.equals(client.getUsername(), inviteAcceptedMessage.getOpponent())) {
						Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " ACCEPTED and invite from " + inviteAcceptedMessage.getOpponent()));
						Thread newGame = getThread(client, clientThread);
						synchronized(activeGames) {
							activeGames.add(newGame);
						}
						newGame.start();
						return;
					}
				} else if (client.activeGame != null && client.isAuthorized()) {
					if (Objects.equals(client.getUsername(), inviteAcceptedMessage.getOpponent())) {
						Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " ACCEPTED and invite from " + inviteAcceptedMessage.getOpponent() + " but they are already in a game!"));
						clientThread.sendMessage(new UserAlreadyInGameMessage());
						return;
					}
				}
			}
		});

		// Handling when a user denies an invitation
		dispatcher.registerHandler(InviteDeniedMessage.class, ((inviteDeniedMessage, clientThread) -> {
			Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " DENIED an invite from " + inviteDeniedMessage.getOpponent() + " to play a game!"));
			for (Server.ClientThread client : serverConnection.clients) {
				if (client.activeGame == null && client.isAuthorized()) {
					if (Objects.equals(client.getUsername(), inviteDeniedMessage.getOpponent())) {
						client.sendMessage(new InviteDeniedMessage(clientThread.getUsername()));
						return;
					}
				}
			}
		}));

		// Handling when a user joins the lobby
		dispatcher.registerHandler(UserLobbyUpdateMessage.class, (userLobbyUpdate, clientThread) -> {
			boolean joinedOrLeftLobby;
			Platform.runLater(() -> listItems.getItems().add(clientThread.getUsername() + " is " + (userLobbyUpdate.isJoinedLobby() ? "JOINING" : "LEAVING") + " the lobby."));
			if (userLobbyUpdate.isJoinedLobby()) {
				serverConnection.lobby.add(clientThread.getUsername());
				joinedOrLeftLobby = true;
			} else {
				serverConnection.lobby.remove(clientThread.getUsername());
				joinedOrLeftLobby = false;
			}
			sendToAllAuthorizedUsers(new UpdateLobbyMessage(clientThread.getUsername(), joinedOrLeftLobby));
		});

		
		listItems = new ListView<>();

		sceneMap = new HashMap<>();
		
		sceneMap.put("server",  createServerGui());
		
		primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

		primaryStage.setScene(sceneMap.get("server"));
		primaryStage.setTitle("This is the Server");
		primaryStage.show();
		
	}

	private Thread getThread(Server.ClientThread clientThread, Server.ClientThread opponent) {
		GameSession session = new GameSession(clientThread, opponent);
		clientThread.activeGame = session;
		opponent.activeGame = session;
		sendToAllAuthorizedUsers(new AvailableUsersMessage(getAvailableUsers()));
        return new Thread(() -> {
            try {
                session.run();
            } finally {
                // remove *this* thread from activeGames
                synchronized(activeGames) {
                    activeGames.remove(Thread.currentThread());
                }
                Platform.runLater(() ->
                        listItems.getItems().add("Game ended: " + Thread.currentThread().getName())
                );
				// Send clients the new list of available users since some left.
				sendToAllAuthorizedUsers(new AvailableUsersMessage(getAvailableUsers()));
            }
        }, "GameSession-" + clientThread.count + "-" + opponent.count);
	}

	public Scene createServerGui() {
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");
		
		pane.setCenter(listItems);
		pane.setStyle("-fx-font-family: 'serif'");
		return new Scene(pane, 500, 400);
	}

	private ArrayList<String> getAvailableUsers() {
		ArrayList<String> availableUsers = new ArrayList<>();
		for (Server.ClientThread client : serverConnection.clients) {
			if (client.isAuthorized() && client.activeGame == null) {
				availableUsers.add(client.getUsername());
			}
		}
		return availableUsers;
	}

	private void sendToAllAuthorizedUsers(Message message) {
		for (Server.ClientThread client : serverConnection.clients) {
			if (client.isAuthorized()) {
				client.sendMessage(message);
			}
		}
	}
}
