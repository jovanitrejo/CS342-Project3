import MessageClasses.Message;
import MessageClasses.QuitMessage;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<>();
	TheServer server;
	ArrayList<ClientThread> waitingForGame = new ArrayList<>();
	private final MessageDispatcher dispatcher;
	private final Consumer<String> guiLogger;


	Server(MessageDispatcher dispatcher, Consumer<String> logger){
		this.dispatcher = dispatcher;
		this.guiLogger = logger;
		server = new TheServer();
		server.start();
	}
	
	
	@SuppressWarnings("InfiniteLoopStatement")
    public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mySocket = new ServerSocket(5555)){
		    System.out.println("Server is waiting for a client!");
		  
			
		    while(true) {
		
				ClientThread c = new ClientThread(mySocket.accept(), count);
				guiLogger.accept("client has connected to server: " + "client #" + count);
				clients.add(c);
				c.start();
				
				count++;
				
			    }
			}//end of try
				catch(Exception e) {
					guiLogger.accept("Server socket did not launch");
				}
			}//end of while
		}
	

		public class ClientThread extends Thread{
			
		
			Socket connection;
			int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			private String username;
			private boolean authorized = false;
			public GameSession activeGame;
			
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}
			
			public void run(){
					
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}

				 while(true) {
					    try {
					    	Message data = (Message) in.readObject();
					    	dispatcher.dispatch(data, this);
					    	}
					    catch(Exception e) {
					    	guiLogger.accept("Oops...Something wrong with the socket from client: " + count + "....closing down!");
					    	clients.remove(this);
                            waitingForGame.remove(this);
							if (this.activeGame != null) {
								// Shuts down an active game if there is one. Sends the other player back to the main menu.
								Platform.runLater(() -> guiLogger.accept("User quit while playing an active game! Sending quit message to other player..."));
								ClientThread opponent = this.activeGame.player1 == this ? activeGame.player2 : activeGame.player1;
								this.activeGame.endGame();
								this.activeGame = null;
								opponent.activeGame = null;
								opponent.sendMessage(new QuitMessage());
							}
							break;
					    }
					}
				}//end of run


            public boolean isAuthorized() {
                return authorized;
            }

            public void setAuthorized(boolean authorized) {
                this.authorized = authorized;
            }

			public String getUsername() {
				return this.username;
			}

			public void sendMessage(Message message) {
				try {
					out.writeObject(message);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			public void setUsername(String username) {
				this.username = username;
			}
        }//end of client thread
}


	
	

	
