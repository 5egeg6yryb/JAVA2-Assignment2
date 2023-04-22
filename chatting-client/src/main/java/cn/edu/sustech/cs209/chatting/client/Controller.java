package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
    @FXML
    Label currentUsername;
    @FXML
    ListView<String> chatList;

    Set<String> chatListName = new HashSet<>();
    @FXML
    TextArea inputArea;
    @FXML
    ListView<Message> chatContentList;

    String username;

    Scanner in = Main.in;
    PrintWriter out = Main.out;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */

            while (true) {
                username = input.get();
                out.println("CheckUser " + username);
                out.flush();

                if (in.nextInt() == 0){

                    currentUsername.setText(username);

                    out.println("AddUser " + username);
                    out.flush();
                    break;
                }
                else{
                    System.out.println("change username");
                    Platform.exit();
                }
            }

        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }

        chatList.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends String> observable, String oldValue, String newValue) ->{
                System.out.println("Jump: " + newValue);
                chatContentList.getItems().clear();
                Main.To = newValue;
                out.println("Jump" + " " + username + " " +newValue);
                out.flush();
            });


        chatContentList.setCellFactory(new MessageCellFactory());
        new Thread(new ClientService(chatContentList, chatList)).start();

    }


    @FXML
    public void createPrivateChat() throws InterruptedException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        out.println("GetUser");
        out.flush();

        Thread.sleep(100);

        String list = Main.list;
        String [] tokens = list.split("@");

        System.out.println("list:");
        System.out.println(list);

        for (String token : tokens) {
            if (!token.equals(username)) {
                userSel.getItems().add(token);
            }
        }
        // userSel.getItems().addAll(names);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name

        Main.To = user.get();

        if ((Main.To!=null)&&(!Main.To.equals("null"))) {
            if (!chatListName.contains(user.get())) {
                chatList.getItems().add(user.get());
                chatListName.add(user.get());
                chatContentList.getItems().clear();
            } else {
                chatContentList.getItems().clear();
                out.println("Jump" + " " + username + " " + Main.To);
                out.flush();
            }
        }


    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() throws InterruptedException {
        AtomicReference<String> user = new AtomicReference<>();
        AtomicReference<String> membersName = new AtomicReference<>();

        Stage stage = new Stage();
        ListView<String> GroupSel = new ListView<>();

        out.println("GetUser");
        out.flush();

        Thread.sleep(100);

        String list = Main.list;
        String[] tokens = list.split("@");

        System.out.println("list:");
        System.out.println(list);

        for (String token : tokens) {
            if (!token.equals(username)) {
                GroupSel.getItems().add(token);
            }
        }

        Button okBtn = new Button("OK");

        GroupSel.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        okBtn.setOnAction(e -> {
            String s = GroupSel.getSelectionModel().getSelectedItems().toString();
            s = s.substring(1, s.length() - 1);
            if (!s.equals("")) {
                s = s.replace(" ", "");
                s += "," + username;
                System.out.println(s);
                membersName.set(s);
                String[] token = s.split(",");
                Arrays.sort(token);
                System.out.println("sort: " + Arrays.toString(token));
                if (token.length == 2) {
                    user.set(token[0] + "," + token[1] + "(2)");
                } else {
                    user.set(
                        token[0] + "," + token[1] + "," + token[2] + "(" + token.length + ")...");
                }

                stage.close();
            }

        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(GroupSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        String members = membersName.get();
        if (members != null) {
            members = members.replace(" ", "");

            if (!chatListName.contains(user.get())) {
                chatList.getItems().add(user.get());
                chatListName.add(user.get());
                Main.To = user.get();
                out.println("BuildGroup " + user + " " + members);
                out.flush();
                System.out.println("BuildGroup " + user + " " + members);

            }
            chatContentList.getItems().clear();
            out.println("Jump" + " " + username + " " + Main.To);
            out.flush();

        }
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException {
        // TODO
        String data = inputArea.getText();
        if (!data.equals("")) {
            if (!Main.To.contains("(")){

            Message message = new Message(System.currentTimeMillis(), username, Main.To, data);
            chatContentList.getItems().add(message);
            inputArea.setText("");

            out.println("Send " + message.getTimestamp() + " " + message.getSentBy() + " "
                + message.getSendTo() + " " + message.getData().replace("\n", "#"));
            out.flush();
            }
            else{
                Message message = new Message(System.currentTimeMillis(), username, Main.To, data);
                chatContentList.getItems().add(message);
                inputArea.setText("");

                out.println("SendGroup " + message.getTimestamp() + " " + message.getSentBy() + " "
                    + message.getSendTo() + " " + message.getData().replace("\n", "#"));
                out.flush();
            }
            if (data.contains("file") && data.contains(".txt")){
                String path = data.split(" ")[1];
                //D:\\Idea project\\JAVA2-Assignment2\\chatting-client\\src\\main\\java\\cn\\edu\\sustech\\cs209\\chatting\\client\\send.txt
                Scanner scanner = new Scanner(
                    Paths.get("D:\\Idea project\\JAVA2-Assignment2\\chatting-client\\src\\main"
                        + "\\java\\cn\\edu\\sustech\\cs209\\chatting\\client\\" + path),
                    StandardCharsets.UTF_8.name());
                String content = scanner.useDelimiter("\\A").next();
                scanner.close();

                System.out.println("content:" + content);


                System.out.println("File " + path + " " + username + " "
                    + Main.To + " " + content.replace("\r\n", "#"));

                out.println("File " + path + " " + username + " "
                    + Main.To + " " + content.replace("\r\n", "#"));
                out.flush();
            }
            if (data.contains("file") && data.contains(".docx")){
                String path = data.split(" ")[1];


            }
        }


    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

}
