package MessageClasses;

public class NewChatMessage extends Message{
    private static final long serialVersionUID = 42L;

    private final String messageSender;
    private final String message;

    public NewChatMessage(String messenger, String message) {
        messageSender = messenger;
        this.message = message;
    }

    public String getMessageSender() {
        return this.messageSender;
    }

    public String getMessage() {
        return this.message;
    }
}
