package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static ArrayList<String> usernames = new ArrayList<>();
    static Map<String, Socket> sockets = new HashMap<>();
    static ArrayList<Message> PrivateMessage = new ArrayList<>();
    static ArrayList<Message> GroupMessage = new ArrayList<>();
    static Map<String, String[]> Group2Member = new HashMap<>();

    public static void main(String[] args) throws IOException {
        final int PORT = 8888;

        ServerSocket server = new ServerSocket(PORT);

        System.out.println("Starting server");

        while(true){
            Socket s = server.accept();
            System.out.println("Client Connected");
            ServerService c = new ServerService(s);
            Thread t = new Thread(c);
            t.start();
            System.out.println("Client Close");

        }
    }
}
