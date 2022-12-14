package com.github.redreaperlp.fourwinsserver.client;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.Answer;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;
import com.github.redreaperlp.fourwinsserver.server.objects.LineBlock;
import com.github.redreaperlp.fourwinsserver.util.Codec;

import java.io.Console;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class PlayersTurnHandler implements Runnable {

    private Socket socket;
    private String address;
    private int port;
    private LineBlock lineBlock;
    private User user;
    private boolean isX;

    private Thread thread;

    private String RED = "\u001B[31m";
    private String RESET = "\u001B[0m";
    private String YELLOW = "\u001B[33m";
    private String GREEN = "\u001B[32m";
    public PlayersTurnHandler(String address, int port, LineBlock lineBlock, User user, boolean isX, Thread thread) {
        this.address = address;
        this.port = port;
        this.lineBlock = lineBlock;
        this.user = user;
        this.isX = isX;
        this.thread = thread;
    }

    @Override
    public void run() {
        Codec codec = new Codec();
        while (true) {
            try {
                Console console = System.console();
                String consoleInput = console.readLine();
                int intInput = Integer.parseInt(consoleInput); //Trys to parse the input to an integer
                socket = new Socket(address, port);
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    user.setCommand(ClientCommand.SET_STONE); //Sets the command to SET_STONE
                    writer.println(codec.userSendString(user, String.valueOf(intInput)) + "\n"); //Sends the input to the server
                    writer.flush();
                    Scanner scanner = new Scanner(socket.getInputStream());
                    String input = scanner.nextLine();
                    Answer answer = codec.toServerAnswer(input); //Gets the answer from the server out of an input string
                    switch (answer.getAnswer()) { //Checks the answer from the server
                        case NOTIFY_CLIENT_GAMESTONESET -> {
                            printLine(20);
                            if (Main.wantColoredConsole) {
                                System.out.println(GREEN + "You set the stone" + RESET);
                            } else {
                                System.out.println("You set the stone");
                            }
                            printLine(2);
                            lineBlock.setRowValue(intInput - 1, isX ? 1 : 2); //Sets the stone in the lineblock
                            lineBlock.print(false);
                            lineBlock.setLastTurn(intInput);
                            GameserverConnection.myTurn = false;
                            return;
                        }
                        case NOT_YOUR_TURN -> {
                            printLine(3);
                            if (Main.wantColoredConsole) {
                                System.out.println(RED + "It's not your turn" + RESET);
                            } else {
                                System.out.println("It's not your turn");
                            }
                            return;
                        }
                        case NOTIFY_CLIENT_INVALIDSET -> {
                            printLine(2);
                            System.out.println("Invalid set");
                            String[] s = answer.getAnswerData().split("\\.");
                            if (Main.wantColoredConsole) {
                                System.out.println(RED + "The input has to be between " + YELLOW + s[0] + RED + " and " + YELLOW + s[1] + RED + "!" + RESET);
                            } else {
                                System.out.println("The input has to be between " + s[0] + " and " + s[1] + "!");
                            }
                        }
                        case COL_FULL -> {
                            printLine(2);
                            System.out.println("Column " + answer.getAnswerData() + " is full, try another one");
                            if (Main.wantColoredConsole) {
                                System.out.println(RED + "Column " + GREEN + answer.getAnswerData() + YELLOW + " is full, try another one" + RESET);
                            } else {
                                System.out.println("Column " + answer.getAnswerData() + " is full, try another one");
                            }
                        }
                        case NOTIFY_CLIENT_WON -> {
                            printLine(20);
                            lineBlock.setRowValue(intInput - 1, isX ? 1 : 2); //Sets the stone in the lineblock
                            lineBlock.setLastTurn(intInput);
                            lineBlock.print(true);
                            printLine(2);
                            if (Main.wantColoredConsole) {
                                System.out.println(GREEN + "You won!" + RESET);
                                System.out.println(YELLOW + "If you want to play again, just rerun the program" + RESET);
                            } else {
                                System.out.println("You won!");
                                System.out.println("If you want to play again, just rerun the program");
                            }
                            TimeUnit.SECONDS.sleep(2);
                            System.exit(0);
                            return;
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } catch (Exception e) { //If the input is not an integer it will ask for a new input
                if (Main.wantColoredConsole) {
                    System.out.println(RED + "Invalid input" + RESET);
                } else {
                    System.out.println("Invalid input");
                }
            }
        }
    }
    public void printLine(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println();
        }
    }
}
