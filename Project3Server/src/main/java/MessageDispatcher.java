import MessageClasses.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
@SuppressWarnings("unchecked")
public class MessageDispatcher {
    private final Map<Class<?>, BiConsumer<Message, Server.ClientThread>> handlers = new HashMap<>();

    public <T extends Message> void registerHandler(Class<T> messageClass, BiConsumer<T, Server.ClientThread> handler) {
        handlers.put(messageClass, (BiConsumer<Message, Server.ClientThread>) handler);
    }

    public void dispatch(Message message, Server.ClientThread clientThread) {
        BiConsumer<Message, Server.ClientThread> handler = handlers.get(message.getClass());
        if (handler != null) {
            handler.accept(message, clientThread);
        } else {
            System.err.println("No handler registered for: " + message.getClass());
        }
    }
}
