
import java.util.ArrayList;
import java.util.HashMap;

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
		serverConnection = new Server(dispatcher, data -> Platform.runLater(()-> listItems.getItems().add(data)));

		// Handle Login Requests on the server
		dispatcher.registerHandler(LoginMessage.class, (loginMessage, clientThread) -> {
			System.out.println("Got a request from the client");
			for(Server.ClientThread client : serverConnection.clients) {
				if (client.isAuthorized() && (client.getUsername().equals(loginMessage.getUsername()))) {
					if (client == clientThread) {
						try {
							clientThread.sendMessage(new LoginResponse(false, "You already set your username to this. Try a new one."));
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
			} catch (Exception e) {
				Platform.runLater(() -> listItems.getItems().add("Error occurred when sending MessageClasses.LoginResponse to client #" + clientThread.count));
			}
		});

		// Handle NewGameRequests from clients
		dispatcher.registerHandler(NewGameMessage.class, ((newGameMessage, clientThread) -> {
			Platform.runLater(() -> listItems.getItems().add("Got a game request! Sending response..."));
			if (!serverConnection.waitingForGame.isEmpty()) {
				Platform.runLater(() -> listItems.getItems().add("Two players wanna play! Starting a new game..."));
				// at least one player in the queue
				Server.ClientThread opponent = serverConnection.waitingForGame.get(0);
				serverConnection.waitingForGame.remove(opponent);
				Thread newGame = getThread(clientThread, opponent);

				synchronized(activeGames) {
					activeGames.add(newGame);
				}
				newGame.start();
			} else {
				Platform.runLater(() -> listItems.getItems().add("A player wants to play, need to find an opponent..."));
				serverConnection.waitingForGame.add(clientThread);
				clientThread.sendMessage(new NewGameResponse());
			}
		}));

		// Handling moves
		dispatcher.registerHandler(MoveMessage.class, (moveMessage, clientThread) -> {
			Platform.runLater(() -> listItems.getItems().add("User made a move! Processing"));
			clientThread.activeGame.onMove(moveMessage, clientThread);
		});

		// Handling quit

		
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
}
