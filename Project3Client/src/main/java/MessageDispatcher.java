import MessageClasses.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class MessageDispatcher {
    private final Map<Class<?>, Consumer<Message>> handlers = new HashMap<>();

    public <T extends Message> void registerHandler(Class<T> concreteClass, Consumer<T> handler) {
        handlers.put(concreteClass, (Consumer<Message>) handler);
    }

    public void dispatch(Message message) {
        Consumer<Message> handler = handlers.get(message.getClass());
        if (handler != null) {
            handler.accept(message);
        } else {
            System.err.println("No handler registered for: " + message.getClass());
        }
    }
}
