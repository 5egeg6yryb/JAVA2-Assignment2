package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.omg.CORBA.SystemException;

public class ServerService implements Runnable {

  private Socket s;
  private Scanner in;
  private PrintWriter out;

  private String username;

  public ServerService(Socket s) {
    this.s = s;
  }

  @Override
  public void run() {
    try {
      try {
        in = new Scanner(s.getInputStream());
        out = new PrintWriter(s.getOutputStream());
        doService();
      } finally {
        System.out.println("client close");
        try {
          Main.usernames.remove(this.username);
        } catch (Exception ignored) {

        }

        s.close();
      }

    } catch (Exception ignored) {
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
      case "AddUser":
        String name = in.next();
        Main.usernames.add(name);
        this.username = name;
        Main.sockets.put(name, this.s);
        System.out.println("AddUser: " + name);
        break;

      case "CheckUser":
        String input = in.next();
        boolean valid = true;
        for (int i = 0; i < Main.usernames.size(); i++) {
          if (Main.usernames.get(i).equals(input)) {
            out.println(1);
            out.flush();
            valid = false;
            System.out.println("Duplicate Username: " + input);
            break;
          }
        }
        if (valid) {
          out.println(0);
          out.flush();
          System.out.println("Valid Username: " + input);
        }
        break;

      case "GetUser":
        String names = "";
        for (int i = 0; i < Main.usernames.size(); i++) {
          names += Main.usernames.get(i) + "@";
        }
        out.println("list " + names);
        out.flush();
        System.out.println("getAllUsers: " + names);
        break;

      case "Send":
        Long timestamp = in.nextLong();
        String sendBy = in.next();
        String sendTo = in.next();
        String data = in.nextLine();

        Message message = new Message(timestamp, sendBy, sendTo, data);
        Main.PrivateMessage.add(message);

        PrintWriter out2other = new PrintWriter(Main.sockets.get(sendTo).getOutputStream());

        out2other.println("Send " + timestamp + " " + sendBy + " " + sendTo + " " + data);
        out2other.flush();
        System.out.println("Send " + timestamp + " " + sendBy + " " + sendTo + " " + data);

        break;

      case "Jump":

        String By = in.next();
        String To = in.next();

        List<Message> messages;
        if (To.contains("(")) {
          String[] users = Main.Group2Member.get(To);
          out.println("Load " + System.currentTimeMillis() + " Server " + By + " " + To + " "
              + Arrays.toString(users));
          out.flush();
          messages = Main.GroupMessage.stream().filter(a -> (a.getSendTo().equals(To)))
              .collect(Collectors.toList());
        } else {
          messages = Main.PrivateMessage.stream()
              .filter(a -> (a.getSentBy().equals(By) && a.getSendTo().equals(To))
                  || (a.getSentBy().equals(To) && a.getSendTo().equals(By)))
              .collect(Collectors.toList());
        }
        LoadMessages(messages);

        break;

      case "BuildGroup":
        String GroupName = in.next();
        String members = in.next();
        String[] tokens = members.split(",");
        Main.Group2Member.put(GroupName, tokens);
        break;

      case "SendGroup":
        Long timestamp2 = in.nextLong();
        String sendBy2 = in.next();
        String sendTo2 = in.next();
        String data2 = in.nextLine();

        Message message2 = new Message(timestamp2, sendBy2, sendTo2, data2);
        Main.GroupMessage.add(message2);

        String[] receivers = Main.Group2Member.get(sendTo2);
        for (String receiver : receivers) {
          if ((!receiver.equals(sendBy2)) && (Main.usernames.contains(receiver))) {
            PrintWriter out2receiver = new PrintWriter(
                Main.sockets.get(receiver).getOutputStream());
            out2receiver.println(
                "SendGroup " + timestamp2 + " " + sendBy2 + " " + sendTo2 + " " + data2);
            out2receiver.flush();
            System.out.println(
                "SendGroup " + timestamp2 + " " + sendBy2 + " " + sendTo2 + " " + data2);
          }
        }

        break;

      case "New":
        long timestamp3 = in.nextLong();
        String sendBy3 = in.next();
        String sendTo3 = in.next();
        String data3 = in.nextLine();

        out.println("Load " + timestamp3 + " " + sendBy3 + " " + sendTo3 + " " + data3);
        out.flush();
        break;

      case "File":
        String file = in.next();
        String sendBy4 = in.next();
        String sendTo4 = in.next();
        String data4 = in.nextLine();

        PrintWriter out2other2 = new PrintWriter(Main.sockets.get(sendTo4).getOutputStream());

        out2other2.println("File " + file + " " + sendBy4 + " " + sendTo4 + " " + data4);
        out2other2.flush();

        break;

    }
  }

  public void LoadMessages(List<Message> messages) {
    if (messages.size() == 0) {
      return;
    }
    for (Message message : messages) {
      out.println(
          "Load " + message.getTimestamp() + " " + message.getSentBy() + " " + message.getSendTo()
              + " " + message.getData());
      System.out.println(
          "Load " + message.getTimestamp() + " " + message.getSentBy() + " " + message.getSendTo()
              + " " + message.getData());
      out.flush();
    }
  }
}