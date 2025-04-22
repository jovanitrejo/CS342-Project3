package MessageClasses;

public class ChatMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final String message;

    public ChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
