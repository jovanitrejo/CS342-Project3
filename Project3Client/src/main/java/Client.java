import MessageClasses.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;



public class Client extends Thread{

	// Objects used to communicate with server
	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;

	// Handler class used to handle specific message types (specifically implemented in GuiClient.java)
	private final MessageDispatcher dispatcher;

	// Constructor
	Client(MessageDispatcher dispatcher){
		this.dispatcher = dispatcher;
	}
	
	public void run() {
		
		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {
			return;
		}
		
		while(true) {
			try {
			Message message = (Message) in.readObject();
			dispatcher.dispatch(message);
			}
			catch(Exception e) {
				return;
			}
		}
    }

	// Sends a Message object to the server
	public void send(Message message) {
		try {
			out.reset();
			out.writeObject(message);
			out.flush();
		} catch (IOException e) {
			System.out.print("An error has occurred sending data to the server");
        }
	}
}