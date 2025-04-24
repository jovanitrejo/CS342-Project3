package MessageClasses;

public class UserLobbyUpdateMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final boolean joinedLobby; // True if joined, false if left

    public UserLobbyUpdateMessage(boolean joined) {
        joinedLobby = joined;
    }

    public boolean isJoinedLobby() {
        return joinedLobby;
    }
}
