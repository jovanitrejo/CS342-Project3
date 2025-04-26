package Scenes;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import utils.CustomJavaFXElementsTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class MainMenu {
    private final StackPane root;
    public final VBox onlineUsersList;
    private final Consumer<String> inviteAcceptedCallback;
    private final Consumer<String> inviteDeniedCallback;
    private final Consumer<String> inviteUserCallback;
    private final HashMap<String, Button> availableUsers = new HashMap<>();
    private final ArrayList<VBox> activeInvitePopUps = new ArrayList<>();
    private final ArrayList<String> pendingInvites = new ArrayList<>();

    public MainMenu(Consumer<Boolean> findNewGameCallback, Consumer<Boolean> changeUsernameCallback, Consumer<String> inviteAcceptedCallback, Consumer<String> inviteDeniedCallback, Consumer<String> inviteUserCallback, Runnable joinFromLobbyCallback) {
        this.inviteAcceptedCallback = inviteAcceptedCallback;
        this.inviteDeniedCallback = inviteDeniedCallback;
        this.inviteUserCallback = inviteUserCallback;
        final VBox welcomeText = CustomJavaFXElementsTools.createCustomLabel("CS342 Connect 4");

        welcomeText.setAlignment(Pos.TOP_CENTER);
        welcomeText.setPadding(new Insets(65, 0, 0, 0));

        Button findNewGameButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Find New Game", 24, false);
        findNewGameButton.setOnAction(e -> findNewGameCallback.accept(true));

        Button joinFromLobbyButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#FFFFFF", Color.BLACK, "Join from lobby", 24, false);
        joinFromLobbyButton.setOnAction(e -> joinFromLobbyCallback.run());

        Button changeUsernameButton = CustomJavaFXElementsTools.createStyledButton(300, 50, "#656565", Color.WHITE, "Change Username", 24, false);
        changeUsernameButton.setOnAction(e -> changeUsernameCallback.accept(true));

        VBox options = new VBox(20, findNewGameButton, joinFromLobbyButton, changeUsernameButton);
        options.setAlignment(Pos.CENTER);

        // List of online users displayed on the center-right of the screen
        onlineUsersList = new VBox();
        onlineUsersList.setSpacing(20);
        Text onlineUsersText = new Text("Active Users");

        ScrollPane scrollableList = new ScrollPane(onlineUsersList);
        VBox fullWindow = new VBox(onlineUsersText, scrollableList);
        fullWindow.setPickOnBounds(false);
        fullWindow.setBackground(new Background(new BackgroundFill(
                Color.web("#7D7D7D", 0.75),
                new CornerRadii(15),
                Insets.EMPTY
        )));
        fullWindow.setMaxSize(275, 400);


        root = new StackPane(welcomeText, options, fullWindow);
        StackPane.setAlignment(welcomeText, Pos.TOP_CENTER);
        StackPane.setAlignment(options, Pos.CENTER);
        StackPane.setAlignment(fullWindow, Pos.CENTER_RIGHT);

        //online user list styles
        onlineUsersText.setFont(Font.font("Londrina Solid", 32));
        onlineUsersText.setStroke(Color.BLACK);
        onlineUsersText.setStrokeWidth(1);
        onlineUsersText.setFill(Color.WHITE);
        onlineUsersList.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        scrollableList.setStyle("-fx-background-color: transparent;");
        scrollableList.getStyleClass().add("userList");
        scrollableList.getStylesheets().add("path/stylesheet.css");
        scrollableList.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollableList.setFitToWidth(true);
        StackPane.setMargin(fullWindow, new Insets(0, 50, 0, 0));

    }

    public void showInvitePopup(String requestorUsername) {
        final VBox[] newInvitePopUp = new VBox[1];
        pendingInvites.add(requestorUsername);

        newInvitePopUp[0] = CustomJavaFXElementsTools.createPopUp(
                () -> {
                    // Case where user invite is denied
                    inviteDeniedCallback.accept(requestorUsername);
                    hideReplayPopUp(newInvitePopUp[0]);
                    pendingInvites.remove(requestorUsername);
                },
                () -> {
                    // Case where user invite is accepted
                    inviteAcceptedCallback.accept(requestorUsername);
                    hideReplayPopUp(newInvitePopUp[0]);
                    pendingInvites.remove(requestorUsername);
                    activeInvitePopUps.remove(newInvitePopUp[0]);
                    hideAllOtherInvitePopUps();

                    // Reject all other invites since we have accepted one
                    for(String invite : pendingInvites) {
                        inviteDeniedCallback.accept(invite);
                    }
                },
                requestorUsername + " has requested to play against you! Do you accept?",
                "No",
                "Yes"
        );
        activeInvitePopUps.add(newInvitePopUp[0]);
        root.getChildren().add(newInvitePopUp[0]);
    }

    private void hideAllOtherInvitePopUps() {
        for (VBox popup : new ArrayList<>(activeInvitePopUps)) {
            root.getChildren().remove(popup);
        }
        activeInvitePopUps.clear();
    }

    private void hideReplayPopUp(VBox newInvitePopUp) {
        ObservableList<Node> kids = root.getChildren();
        kids.remove(newInvitePopUp);
    }

    public void showRequestorIsAlreadyInGame() {
        final VBox[] newNotification = new VBox[1];
        newNotification[0] = CustomJavaFXElementsTools.createNotification(
                () -> hideReplayPopUp(newNotification[0]),
                "The user you accepted an invite from is already in a game!"
        );
        root.getChildren().add(newNotification[0]);
    }

    public void updateActiveUsers(ArrayList<String> usernames, String myUsername) {
        // Remove the existing list of users
        System.out.println("Updating active users");
        onlineUsersList.getChildren().clear();
        availableUsers.clear();

        // Add full-list of current users to list
        for(String username : usernames) {
            if (!Objects.equals(username, myUsername)) {
                Label usernameLabel = new Label();
                usernameLabel.setText(username);
//                Text usernameText = new Text(username);
//                usernameText.setFont(Font.font("Londrina Solid", 20));
//                usernameText.setStroke(Color.BLACK);
//                usernameText.setFill(Color.WHITE);
//                usernameText.setStrokeWidth(1);
                Button thisButton = CustomJavaFXElementsTools.createStyledButton(60, 25, "#00B2FF", Color.WHITE, "Invite", 16, true);
                thisButton.setOnAction(e -> {
                    System.out.println("Sending invite to: " + username);
                    inviteUserCallback.accept(username);
                    disableInviteButton(username);
                });
                availableUsers.put(username, thisButton);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox users = new HBox(usernameLabel, spacer, thisButton);
                usernameLabel.setMaxWidth(100);
                usernameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                usernameLabel.setStyle("-fx-stroke-width: 1px;");
                usernameLabel.setTextFill(Color.WHITE);
                usernameLabel.setFont(Font.font("Londrina Solid", 20));
                users.setPrefWidth(257);
                users.setPrefHeight(30);
                onlineUsersList.getChildren().add(users);
            }
        }
        System.out.println("Current available number of users: " + availableUsers.size());
        for (String username : availableUsers.keySet()) {
            System.out.println(username);
        }
    }

    public void reEnableInviteButton(String username) {
        System.out.println("Trying to re-enable: " + username);
        if (!availableUsers.containsKey(username)) {
            System.out.println("No such user found in map!");
            return;
        }
        Button usersButton = availableUsers.get(username);
        usersButton.setDisable(false);
    }

    public void disableInviteButton(String username) {
        System.out.println("Trying to disable: " + username);
        if (!availableUsers.containsKey(username)) {
            System.out.println("No such user found in map!");
            return;
        }
        Button usersButton = availableUsers.get(username);
        usersButton.setDisable(true);
    }

    public StackPane getRoot() {
        return this.root;
    }
}
