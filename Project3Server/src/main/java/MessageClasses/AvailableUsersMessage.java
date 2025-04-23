package MessageClasses;

import java.util.ArrayList;

public class AvailableUsersMessage extends Message {
    private static final long serialVersionUID = 42L;
    private final ArrayList<String> availableUsers;

    public AvailableUsersMessage(ArrayList<String> availableUsers) {
        this.availableUsers = availableUsers;
    }

    public ArrayList<String> getActiveUsers() {
        return this.availableUsers;
    }
}
