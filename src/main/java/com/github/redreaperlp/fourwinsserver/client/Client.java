package com.github.redreaperlp.fourwinsserver.client;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.Answer;
import com.github.redreaperlp.fourwinsserver.objects.Config;
import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.util.Codec;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private int port;
    Config config = new Config();

    public Client() {
        Codec codec = new Codec();
        Main main = new Main();
        System.out.println("Connecting to server...");
        try {
            Socket socket = new Socket(Main.config.getConfig(ConfigType.SERVER_ADDRESS.value()), Integer.parseInt(Main.config.getConfig(ConfigType.SERVER_PORT.value())));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(codec.userSendString(config.getConfig(ConfigType.PLAYER_NAME.value()), config.getConfig(ConfigType.PLAYER_PASSWORD.value()), ClientCommand.ASK_FOR_SERVER));
            writer.flush();
            Scanner scanner = new Scanner(socket.getInputStream());
            String input = scanner.nextLine();
            Answer answer = codec.toServerAnswer(input);
            switch (answer.getAnswer()) {
                case NOTIFY_CLIENT_CHANGEPORT:
                    String[] data = answer.getAnswerData().split("_");
                    port = Integer.parseInt(data[0]);
                    boolean iAmX_SET = false;
                    boolean iAmX = false;
                    if (data.length > 1) {
                        iAmX = Boolean.parseBoolean(data[1]);
                        iAmX_SET = true;
                    }
                    System.out.println("Change Port: " + port);
                    GameserverConnection gameserverConnection = new GameserverConnection();
                    gameserverConnection.port = port;
                    gameserverConnection.name = config.getConfig(ConfigType.PLAYER_NAME.value());
                    gameserverConnection.password = config.getConfig(ConfigType.PLAYER_PASSWORD.value());
                    if (iAmX_SET) {
                        gameserverConnection.iAmX = iAmX;
                    }
                    Thread gameServer = new Thread(gameserverConnection);
                    gameServer.start();
                    break;
                case NO_GAMES_AVAILABLE:
                    System.out.println("No Games available");
                    break;
                case GAME_FULL:
                    System.out.println("Game is full");
                    break;
                case JOIN_NOT_ALLOWED:
                    System.out.println("It seems like you configured the server settings wrong (port)");
                    break;
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Server is not available");
            System.exit(0);
        }
    }
}



