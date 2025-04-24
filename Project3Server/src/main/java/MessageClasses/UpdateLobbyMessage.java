package MessageClasses;

public class UpdateLobbyMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final String user;
    private final boolean joinedLobby; // True if joined, false if left

    public UpdateLobbyMessage(String user, boolean joinedLobby) {
        this.user = user;
        this.joinedLobby = joinedLobby;
    }

    public String getUser() {
        return this.user;
    }

    public boolean isJoinedLobby() {
        return joinedLobby;
    }
}
