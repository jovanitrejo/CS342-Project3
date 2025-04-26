package MessageClasses;

public class PlayingOfflineMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final boolean isPlayingOffline;

    public PlayingOfflineMessage(boolean isPlayingOffline) {
        this.isPlayingOffline = isPlayingOffline;
    }

    public boolean isPlayingOffline() {
        return this.isPlayingOffline;
    }
}
