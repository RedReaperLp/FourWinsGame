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
import java.util.concurrent.TimeUnit;

public class Client {
    private int port;
    Config config = new Config();
    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";
    String YELLOW = "\u001B[33m";
    String GREEN = "\u001B[32m";

    public Client() {
        Codec codec = new Codec();
        Main main = new Main();
        if (Main.wantColoredConsole) {
            System.out.println(GREEN + "Connecting to server..." + RESET);
        } else {
            System.out.println("Connecting to server...");
        }
        try {
            Socket socket = new Socket(Main.config.getConfig(ConfigType.SERVER_ADDRESS.value()), Integer.parseInt(Main.config.getConfig(ConfigType.SERVER_PORT.value())));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(codec.userSendString(config.getConfig(ConfigType.PLAYER_NAME.value()), config.getConfig(ConfigType.PLAYER_PASSWORD.value()), ClientCommand.ASK_FOR_SERVER, String.valueOf(Main.localeVersion)));
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
                        iAmX = Boolean.parseBoolean(data[1].split(";")[0]);
                        iAmX_SET = true;
                    }
                    if (Main.wantColoredConsole) {
                        System.out.println(GREEN + "Connected to server" + RESET);
                        System.out.println(GREEN + "Change Port: " + RED + port + RESET);
                    } else {
                        System.out.println("Connected to server");
                        System.out.println("Change Port: " + port);
                    }
                    for (String s : data) {
                        if (s.contains(";")) {
                            String[] data2 = data[1].split(";");
                            if (data2[1].equals("wait")) {
                                if (Main.wantColoredConsole) {
                                    System.out.println(YELLOW + "Waiting for a second player..." + RESET);
                                } else {
                                    System.out.println("Waiting for a second player...");
                                }
                            } else if (data2[1].equals("welcome")) {
                                if (Main.wantColoredConsole) {
                                    System.out.println(YELLOW + "Your opponent starts," + GREEN + " Good Luck!" + RESET);
                                } else {
                                    System.out.println("Your opponent starts, Good Luck!");
                                }
                            } else if (data2[1].equals("welcomeBack")) {
                                if (Main.wantColoredConsole) {
                                    System.out.println(GREEN + "Welcome back!" + YELLOW + " You had an Timeout or you rejoined the game. :D" + RESET);
                                } else {
                                    System.out.println("Welcome back! You had an Timeout or you rejoined the game. :D");
                                }
                            }
                        }
                    }
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
                    if (Main.wantColoredConsole) {
                        System.out.println(RED + "No Games available" + RESET);
                    } else {
                        System.out.println("No Games available");
                    }
                    break;
                case GAME_FULL:
                    if (Main.wantColoredConsole) {
                        System.out.println(RED + "Game is full" + RESET);
                    } else {
                        System.out.println("Game is full");
                    }
                    break;
                case JOIN_NOT_ALLOWED:
                    if (Main.wantColoredConsole) {
                        System.out.println(RED + "It seems like you configured the server settings wrong (" + YELLOW + "port" + RED + ")" + RESET);
                    } else {
                        System.out.println("It seems like you configured the server settings wrong (port)");
                    }
                    break;
                case NOTIFY_CLIENT_WRONGVERSION:
                    String version = "";
                    for (char c : answer.getAnswerData().toCharArray()) {
                        version += c + ".";
                    }
                    if (Main.wantColoredConsole) {
                        System.out.println(RED + "Wrong version, please use version " + YELLOW + version.substring(0, version.length() - 1) + RED + "!" + RESET);
                    } else {
                        System.out.println("Wrong version, please use version " + version.substring(0, version.length() - 1) + "!");
                    }
                    break;
                case ALREADY_LOGGED_IN:
                    if (Main.wantColoredConsole) {
                        System.out.println(RED + "You are already logged in from another location!" + RESET);
                    } else {
                        System.out.println("You are already logged in from another location!");
                    }
                    TimeUnit.SECONDS.sleep(3);
                    System.exit(0);
                    break;
            }
            socket.close();
        } catch (IOException e) {
            if (Main.wantColoredConsole) {
                System.out.println(RED + "Server is not available" + RESET);
            } else {
                System.out.println("Server is not available");
            }
            System.exit(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}



