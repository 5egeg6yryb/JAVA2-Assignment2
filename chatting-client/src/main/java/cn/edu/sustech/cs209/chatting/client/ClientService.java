package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ClientService implements Runnable {

  private Scanner in;
  private PrintWriter out;

  @FXML
  ListView<Message> chatContentList;

  @FXML
  ListView<String> chatList;

  public ClientService(ListView<Message> chat, ListView<String> chatList) {
    chatContentList = chat;
    this.chatList = chatList;
  }

  @Override
  public void run() {
    try {
      try {
        in = Main.in;
        out = Main.out;
        doService();
      } finally {
        Main.s.close();
        System.out.println("Server Closed");
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            chatContentList.getItems().add(new Message(System.currentTimeMillis(),
                "server", "client", "Server ShutDown"));
          }
        });

      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public void doService() throws IOException {
    while (true) {
      if (!in.hasNext()) {
        return;
      }
      String command = in.next();
      executeCommand(command);

    }
  }

  public void executeCommand(String commend) throws IOException {
    switch (commend) {
      case "list":
        Main.list = in.next();
        System.out.println("Main.list:" + Main.list);
        break;

      case "Send":
        Long timestamp = in.nextLong();
        String sendBy = in.next();
        String sendTo = in.next();
        String data = in.nextLine();

        Message message = new Message(timestamp, sendBy, sendTo, data.replace("#", "\n"));
        if (Main.To.equals(sendBy)) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              chatContentList.getItems().add(message);
            }
          });
        }
        else{
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              if (!chatList.getItems().contains(sendBy)){
                chatList.getItems().add(sendBy);
              }
              for (int i = 0; i<chatList.getItems().size();i++){
                if (chatList.getItems().get(i).equals(sendBy)){
                  chatList.getSelectionModel().select(i);
                  Main.To = chatList.getItems().get(i);
                  break;
                  // tell server the message is new, then "server" send you have a new message
                }
              }
            }
          });
        }
        break;

      case "Load":
        Long timestamp2 = in.nextLong();
        String sendBy2 = in.next();
        String sendTo2 = in.next();
        String data2 = in.nextLine();
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            chatContentList.getItems().add(new Message(timestamp2, sendBy2, sendTo2,
                data2.replace("#", "\n")
            ));
          }
        });

        break;

      case "SendGroup":
        Long timestamp3 = in.nextLong();
        String sendBy3 = in.next();
        String sendTo3 = in.next();
        String data3 = in.nextLine();

        Message message3 = new Message(timestamp3, sendBy3, sendTo3, data3.replace("#", "\n"));
        if (Main.To.equals(sendTo3)){
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              chatContentList.getItems().add(message3);
            }
          });
        }
        else{
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              if (!chatList.getItems().contains(sendTo3)){
                chatList.getItems().add(sendTo3);
              }
              for (int i = 0; i<chatList.getItems().size();i++){
                if (chatList.getItems().get(i).equals(sendTo3)){
                  break;

                }
              }
            }
          });
        }

        break;

    }


  }


}