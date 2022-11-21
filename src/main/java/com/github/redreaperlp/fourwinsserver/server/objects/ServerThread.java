package com.github.redreaperlp.fourwinsserver.server.objects;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.objects.enums.ServerAnswer;
import com.github.redreaperlp.fourwinsserver.server.InitServer;
import com.github.redreaperlp.fourwinsserver.server.Timeouter;
import com.github.redreaperlp.fourwinsserver.util.Codec;
import com.github.redreaperlp.fourwinsserver.util.Player;
import com.github.redreaperlp.fourwinsserver.util.SPCmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerThread implements Runnable {
    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";
    String YELLOW = "\u001B[33m";
    String GREEN = "\u001B[32m";

    private boolean gameStarted = false;
    private ServerSocket serverSocket;
    private int port;
    private boolean someoneReconnected = false;
    private int gameID;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> pinged = new ArrayList<>();
    private Socket socket;

    private LineBlock lineBlock;
    private boolean waitingForPlayerSend = false;
    private boolean fieldFull = false;
    private int lastTurn = 0;

    /**
     * Constructor
     *
     * @param serverSocket the server socket the server is listening on
     */
    public ServerThread(ServerSocket serverSocket, int gameID) {
        int height = Integer.parseInt(Main.config.getConfig(ConfigType.FIELD_HEIGHT.value()));
        int width = Integer.parseInt(Main.config.getConfig(ConfigType.FIELD_WIDTH.value()));
        this.lineBlock = new LineBlock(height, width);
        this.serverSocket = serverSocket;
        this.gameID = gameID;
    }

    /**
     * Thread method to listen for new connections asynchronously
     */
    @Override
    public void run() {
        Codec codec = new Codec();
        Timeouter timeouter = new Timeouter();
        InitServer init = new InitServer();
        while (true) {
            socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                if (e.getMessage().equals("Socket closed")) {
                    break;
                }
                throw new RuntimeException(e);
            }
            if (socket != null) {
                String input = null;
                boolean validTurn = false;
                try {
                    input = new Scanner(socket.getInputStream()).nextLine();
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    //TODO: Command handling
                    User user = new Codec().toUser(input);
                    SPCmd spCmd = codec.toSPCmd(input);
                    if (spCmd.command() == ClientCommand.ASK_FOR_SERVER) {
                        writer.println(codec.userSendString(ServerAnswer.JOIN_NOT_ALLOWED));
                        writer.flush();
                    } else {
                        switch (spCmd.command()) {
                            case PING -> {
                                if (containsPlayer(user.name())) {
                                    Player player = getPlayer(user.name());
                                    Player otherPlayer = getOtherPlayer(player);
                                    boolean somethingPrinted = false;
                                    fieldFull = lineBlock.isFull();
                                    if (fieldFull) {
                                        writer.println(codec.userSendString(ServerAnswer.FIELD_FULL));
                                        writer.flush();
                                        pinged.add(player);
                                        if (pinged.size() == 2) {
                                            init.getServer(gameID).close();
                                            System.out.println(YELLOW + "Game " + gameID + " closed because the field is full." + RESET);
                                        }
                                    } else {
                                        if (otherPlayer != null) {
                                            if (otherPlayer.won()) {
                                                writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_OPPONENTWON, otherPlayer.name() + ";" + lastTurn + ";" + lineBlock.toSendable()));
                                                writer.flush();
                                                somethingPrinted = true;
                                                init.getServer(gameID).close();
                                                System.out.println(YELLOW + "Game " + gameID + " closed because " + otherPlayer.name() + " won." + RESET);
                                            }
                                        }
                                        if (lineBlock.isFull() && !somethingPrinted) {
                                            writer.println(codec.userSendString(ServerAnswer.FIELD_FULL));
                                            writer.flush();
                                        }
                                        User timeoutedUser = null;
                                        if (otherPlayer != null && !somethingPrinted) {
                                            timeoutedUser = timeouter.getByName(otherPlayer.name());
                                            if (timeoutedUser != null) {
                                                if (timeoutedUser.isTimedOut()) {
                                                    someoneReconnected = true;
                                                }
                                                if (timeoutedUser.isTimedOut()) {
                                                    writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_TIMEDOUT, timeoutedUser.name()));
                                                    writer.flush();
                                                    somethingPrinted = true;
                                                }
                                            }
                                        }
                                        timeouter.resetTimeout(user);
                                        if (someoneReconnected) {
                                            writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIEND_RECONNECT, timeoutedUser.name()));
                                            writer.flush();
                                            someoneReconnected = false;
                                            somethingPrinted = true;
                                        }
                                        if (gameStarted) {
                                            for (Player p : players) {
                                                if (p.name().equals(user.name())) {
                                                    if (p.isMyTurn()) {
                                                        writer.println(codec.userSendString(ServerAnswer.YOUR_TURN, lineBlock.toSendable()));
                                                        writer.flush();
                                                        somethingPrinted = true;
                                                    }
                                                }
                                            }
                                        }
                                        if (!gameStarted) {
                                            if (players.size() == 2) {
                                                if (Main.wantColoredConsole) {
                                                    System.out.println(YELLOW + "Game " + RED + gameID + YELLOW + " started!" + RESET);
                                                } else {
                                                    System.out.println("Game " + gameID + " started!");
                                                }
                                                writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_GAMESTART));
                                                writer.flush();
                                                somethingPrinted = true;
                                                gameStarted = true;
                                            } else {
                                                if (!waitingForPlayerSend) {
                                                    if (Main.wantColoredConsole) {
                                                        System.out.println(YELLOW + "Waiting for second player..." + RESET);
                                                    } else {
                                                        System.out.println("Waiting for second player...");
                                                    }
                                                    writer.println(codec.userSendString(ServerAnswer.WAITING_FOR_PLAYER));
                                                    writer.flush();
                                                    somethingPrinted = true;
                                                    waitingForPlayerSend = true;
                                                }
                                            }
                                        }
                                    }
                                    if (!somethingPrinted) {
                                        writer.println(codec.userSendString(ServerAnswer.SOMETHING_ELSE));
                                        writer.flush();
                                    }

                                }
                            }
                            case SET_STONE -> {
                                int pos = Integer.parseInt(spCmd.specifiedCommand());
                                pos -= 1;
                                if (pos < 0 || pos > lineBlock.getRowSize() - 1) {
                                    writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_INVALIDSET, "1." + lineBlock.getRowSize()));
                                    writer.flush();
                                } else if (containsPlayer(user.name())) {
                                    Player player = getPlayer(user.name());
                                    if (player.isMyTurn()) {
                                        int playerSign = player.sign();
                                        boolean[] setStone = lineBlock.setRowValue(pos, playerSign);
                                        if (setStone[0]) {
                                            if (setStone[1]) {
                                                player.won(true);
                                                writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_WON, lineBlock.toSendable()));
                                                writer.flush();
                                            } else {
                                                validTurn = true;
                                                Player otherPlayer = getOtherPlayer(player);
                                                otherPlayer.changeTurn();
                                                player.changeTurn();
                                                writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_GAMESTONESET));
                                                writer.flush();
                                            }
                                        } else {
                                            validTurn = false;
                                            writer.println(codec.userSendString(ServerAnswer.COL_FULL, String.valueOf(pos + 1)));
                                            writer.flush();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (input != null) {
                        switch (spCmd.command()) {
                            case SET_STONE -> {
                                int pos = 0;
                                try {
                                    pos = Integer.parseInt(spCmd.specifiedCommand());
                                } catch (NumberFormatException e) {
                                    writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_INVALIDSET, "1." + lineBlock.getRowSize()));
                                    writer.flush();
                                }
                                if (validTurn) {
                                    if (Main.wantColoredConsole) {
                                        System.out.println(YELLOW + "User " + GREEN + user.name() + YELLOW + " set stone in column " + RED + spCmd.specifiedCommand() + YELLOW + " in game " + GREEN + gameID + YELLOW + "." + RESET);
                                    } else {
                                        System.out.println("User " + user.name() + " set stone in column " + spCmd.specifiedCommand() + " in game " + gameID + ".");
                                    }
                                } else if (pos <= 0 || pos > lineBlock.getRowSize()) {
                                    if (Main.wantColoredConsole) {
                                        System.out.println(YELLOW + "User " + GREEN + user.name() + YELLOW + " tried to set stone in column " + RED + spCmd.specifiedCommand() + YELLOW + " in game " + GREEN + gameID + YELLOW + ", but it was invalid." + RESET);
                                    } else {
                                        System.out.println("User " + user.name() + " tried to set stone in column " + spCmd.specifiedCommand() + " in game " + gameID + ", but it was invalid.");
                                    }
                                } else {
                                    if (Main.wantColoredConsole) {
                                        System.out.println(YELLOW + "User " + GREEN + user.name() + YELLOW + " tried to set stone in column " + RED + spCmd.specifiedCommand() + YELLOW + " in game " + GREEN + gameID + YELLOW + ", but column was full." + RESET);
                                    } else {
                                        System.out.println("User " + user.name() + " tried to set stone in column " + spCmd.specifiedCommand() + " in game " + gameID + ", but column was full.");
                                    }
                                }
                            }
                        }
                    } else {
                        System.exit(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }

    }

    /**
     * @param name the name of the player to check containing
     * @return true if the player is already in the list, false if not
     */
    public boolean containsPlayer(String name) {
        Player player = getPlayer(name);
        return player != null;
    }

    public Player getPlayer(String name) {
        for (Player player : players) {
            if (player.name().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public Player getOtherPlayer(Player player) {
        for (Player p : players) {
            if (!p.name().equals(player.name())) {
                return p;
            }
        }
        return null;
    }

    /**
     * Adds a player to the list
     *
     * @param player the name of the player to add
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Get size of players list
     *
     * @return returns the size of the player list
     */
    public int size() {
        return players.size();
    }

    public ArrayList<Player> players() {
        return players;
    }

    public ServerSocket serverSocket() {
        return serverSocket;
    }
}
