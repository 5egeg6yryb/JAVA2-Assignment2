package cn.edu.sustech.cs209.chatting.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    static Socket s;
    static String To = "null";

    static {
        try {
            s = new Socket("localhost", 8888);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static InputStream inputStream;

    static {
        try {
            inputStream = s.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static OutputStream outputStream;

    static {
        try {
            outputStream = s.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Scanner in = new Scanner(inputStream);
    static PrintWriter out = new PrintWriter(outputStream);
    static String list = "null";


    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();

    }
}
