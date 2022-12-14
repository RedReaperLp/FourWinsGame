package com.github.redreaperlp.fourwinsserver.client;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.Answer;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.server.objects.LineBlock;
import com.github.redreaperlp.fourwinsserver.util.Codec;
import com.github.redreaperlp.fourwinsserver.util.SPCmd;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GameserverConnection implements Runnable {
    public boolean timedOutNotified = false;
    public static boolean myTurn = false;
    public boolean gameStarted = false;
    public boolean iAmX = false;
    public Socket socket = null;
    public LineBlock lineBlock;
    public SPCmd spCmd = null;
    public String password;
    public String name;
    String address;
    public int port;
    public static boolean shouldStop = false;

    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";
    String YELLOW = "\u001B[33m";
    String GREEN = "\u001B[32m";

    /**
     * Thread for the connection to the gameserver
     */
    @Override
    public void run() {
        password = Main.config.getConfig(ConfigType.PLAYER_PASSWORD.value());
        address = Main.config.getConfig(ConfigType.SERVER_ADDRESS.value());
        Codec codec = new Codec();
        while (!shouldStop) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                socket = new Socket(address, port);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println(codec.userSendString(new User(name, password, ClientCommand.PING)));
                writer.flush();
                Scanner scanner = new Scanner(socket.getInputStream());
                String input = scanner.nextLine();
                Answer answer = codec.toServerAnswer(input);
                switch (answer.getAnswer()) {
                    case NOTIFY_CLIENT_OPPONENTWON -> {
                        String playerName = answer.getAnswerData().split(";")[0];
                        int lastTurn = Integer.parseInt(answer.getAnswerData().split(";")[1]);
                        lineBlock = new LineBlock(answer.getAnswerData().split(";")[2]);
                        printLine(20);
                        lineBlock.setLastTurn(lastTurn);
                        lineBlock.print(true);
                        printLine(2);
                        if (Main.wantColoredConsole) {
                            System.out.println(RED + "You lost against " + YELLOW + playerName + RED + "!" + RESET);
                        } else {
                            System.out.println("You lost against " + playerName + "!");
                        }
                        shouldStop = true;
                        reset();
                        return;
                    }
                    case NOTIFY_CLIENT_GAMESTART -> {
                        printLine(2);
                        if (Main.wantColoredConsole) {
                            System.out.println(GREEN + "Game is starting now" + RESET);
                        } else {
                            System.out.println("Game is starting now");
                        }
                        gameStarted = true;
                    }
                    case NOTIFY_CLIENT_TIMEDOUT -> {
                        if (!timedOutNotified) {
                            printLine(4);
                            if (Main.wantColoredConsole) {
                                System.out.println(RED + "Player " + YELLOW + answer.getAnswerData() + RED + " timed out");
                                timedOutNotified = true;
                            } else {
                                System.out.println("Player " + answer.getAnswerData() + " timed out");
                                timedOutNotified = true;
                            }
                        }
                    }
                    case NOTIFY_CLIENT_RECONNECT -> {
                        if (timedOutNotified) {
                            if (Main.wantColoredConsole) {
                                System.out.println(GREEN + "Player " + YELLOW + answer.getAnswerData() + GREEN + " reconnected" + RESET);
                            } else {
                                System.out.println("Player " + answer.getAnswerData() + " reconnected");
                            }
                            timedOutNotified = false;
                        }
                    }
                    case YOUR_TURN -> {
                        if (!gameStarted) {
                            printLine(2);
                            if (Main.wantColoredConsole) {
                                System.out.println(GREEN + "Game is starting now" + RESET);
                            } else {
                                System.out.println("Game is starting now");
                            }
                            gameStarted = true;
                        }
                        if (!myTurn) {
                            printLine(20);
                            String[] answerDatas = answer.getAnswerData().split(";");
                            lineBlock = new LineBlock(answerDatas[0], Integer.parseInt(answerDatas[1]));
                            printLine(2);
                            lineBlock.print(true);
                            if (Main.wantColoredConsole) {
                                System.out.print(GREEN + "It's your turn: " + RESET);
                            } else {
                                System.out.print("It's your turn: ");
                            }
                            PlayersTurnHandler playersTurnHandler = new PlayersTurnHandler(address, port, lineBlock, new User(name, password, ClientCommand.SET_STONE), iAmX, Thread.currentThread());
                            new Thread(playersTurnHandler).start();
                            myTurn = true;
                        }
                    }
                    case FIELD_FULL -> {
                        if (Main.wantColoredConsole) {
                            printLine(20);
                            System.out.println(RED + "Field is full" + RESET);
                        } else {
                            printLine(20);
                            System.out.println("Field is full");
                        }
                        reset();
                    }
                    case WAITING_FOR_PLAYER -> {
                        printLine(20);
                        System.out.println("Waiting for player");
                        iAmX = true;
                    }
                    case SOMETHING_ELSE -> {
                    }
                }
                socket.close();
            } catch (Exception e) {
                printLine(20);
                if (Main.wantColoredConsole) {
                    System.out.println(RED + "Gameserver is not available" + RESET);
                    System.out.println(RED + "Connection refused" + RESET);
                } else {
                    System.out.println("Gameserver is not available");
                    System.out.println("Connection refused");
                }
                System.exit(0);
            }
        }
        reset();
    }

    public void reset() {
        printLine(3);
        if (Main.wantColoredConsole) {
            System.out.println(YELLOW + "if you want to play again, please restart the program" + RESET);
        } else {
            System.out.println("if you want to play again, please restart the program");
        }
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    public void printLine(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println();
        }
    }
}
