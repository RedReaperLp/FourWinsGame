package com.github.redreaperlp.fourwinsserver.server;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.objects.enums.ServerAnswer;
import com.github.redreaperlp.fourwinsserver.server.objects.Server;
import com.github.redreaperlp.fourwinsserver.util.Codec;
import com.github.redreaperlp.fourwinsserver.util.Player;
import com.github.redreaperlp.fourwinsserver.util.SPCmd;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class LandingServer {

    InitServer initServer = new InitServer();
    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";
    String YELLOW = "\u001B[33m";
    String GREEN = "\u001B[32m";

    /**
     * Starts the server where the clients can connect to and wait for a game
     *
     * @param port       the port the server will listen on and start to distribute the clients to ports
     * @param maxServers the maximum amount of servers that can be started
     */
    public LandingServer(int port, int maxServers) {
        Timeouter timeouter = new Timeouter();
        Codec codec = new Codec();
        for (int i = port + 1; i < port + maxServers + 1; i++) {
            initServer.serverPorts.add(i);
        }
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            String input = null;
            Thread to = new Thread(timeouter);
            to.start();
            while (true) {
                Socket socket = serverSocket.accept();
                Scanner scanner = new Scanner(socket.getInputStream());
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                input = scanner.nextLine();
                User user = new Codec().toUser(input);
                SPCmd spCmd = new SPCmd(user.command(), user.specifiedCommand());
                //TODO: Check if the user entered the correct password via the database
                if (spCmd.command() != null) {
                    switch (spCmd.command()) {
                        case ASK_FOR_SERVER -> {
                            boolean foundPlayerInGame = false;
                            //This part is for the server to check if the user already has a server
                            int version = 0;
                            try {
                                version = Integer.parseInt(spCmd.specifiedCommand());
                            } catch (NumberFormatException e) {
                            }
                            if (version == Main.localeVersion) {
                                Player player = null;
                                for (Server server : initServer.servers) {
                                    if (server.containsPlayer(user.name()) && server.isClosed() == false) {
                                        foundPlayerInGame = true;
                                        int sign = server.serverThread().getPlayer(user.name()).sign();
                                        boolean isX = sign == 1;
                                        User us = timeouter.getByName(user.name());
                                        if (!us.isTimedOut()) {
                                            writer.println(codec.userSendString(ServerAnswer.ALREADY_LOGGED_IN));
                                            writer.flush();
                                            if (Main.wantColoredConsole) {
                                                System.out.println(YELLOW + "User " + GREEN + user.name() + YELLOW + " tried to connect to the server from another destination" + RESET);
                                            } else {
                                                System.out.println("User " + user.name() + " tried to connect to the server from another destination");
                                            }
                                        } else {
                                            writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_CHANGEPORT, server.port() + "_" + isX + ";welcomeBack"));
                                            writer.flush();
                                            User rcUser = timeouter.getByName(user.name());
                                            if (rcUser.isTimedOut()) {
                                                server.serverThread().getPlayer(user.name()).reconnected(true);
                                                if (Main.wantColoredConsole) {
                                                    System.out.println(YELLOW + "User " + GREEN + user.name() + YELLOW + " reconnected to the server" + RESET);
                                                } else {
                                                    System.out.println("User " + user.name() + " reconnected to the server");
                                                }
                                            } else {
                                                if (Main.wantColoredConsole) {
                                                    System.out.println(GREEN + "Found player in game: " + YELLOW + user.name() + GREEN + " - in Server ID: " + YELLOW + server.gameID() + RESET);
                                                } else {
                                                    System.out.println("Found player in game: " + user.name() + " - in Server ID: " + server.gameID());
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!foundPlayerInGame) {
                                    for (Server server : initServer.servers) {
                                        if (server.size() < 2) {
                                            server.addPlayer(new Player(user.name(), false, 2));
                                            timeouter.addUser(user);
                                            user.setServer(server);
                                            writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_CHANGEPORT, String.valueOf(server.port() + "_" + "false;welcome")));
                                            writer.flush();
                                            foundPlayerInGame = true;
                                            if (Main.wantColoredConsole) {
                                                System.out.println(GREEN + "Added player to game: " + YELLOW + user.name() + GREEN + " on ID: " + YELLOW + server.gameID() + RESET);
                                            } else {
                                                System.out.println("Added player to game: " + user.name() + " on ID: " + server.gameID());
                                            }
                                        }
                                    }
                                    if (!foundPlayerInGame) {
                                        if (initServer.servers.size() < maxServers) {
                                            Server server = new Server(initServer.serverPorts.get(0), initServer.servers.size() + 1);
                                            initServer.servers.add(server);
                                            initServer.serverPorts.remove(0);
                                            player = new Player(user.name(), true, 1);
                                            server.addPlayer(player);
                                            user.setServer(server);
                                            timeouter.addUser(user);
                                            writer.println(new Codec().userSendString(ServerAnswer.NOTIFY_CLIENT_CHANGEPORT, String.valueOf(server.port() + "_" + "true;wait")));
                                            writer.flush();
                                            if (Main.wantColoredConsole) {
                                                System.out.println(GREEN + "Server started on port " + YELLOW + server.port() + GREEN + " with gameID: " + YELLOW + server.gameID() + GREEN + " by the Player: " + YELLOW + user.name() + RESET);
                                            } else {
                                                System.out.println("Server started on port " + server.port() + " with gameID: " + server.gameID() + " by the Player: " + user.name());
                                            }
                                        } else {
                                            writer.println(new Codec().userSendString(ServerAnswer.NO_GAMES_AVAILABLE));
                                            writer.flush();
                                        }
                                    }
                                }
                            } else {
                                writer.println(codec.userSendString(ServerAnswer.NOTIFY_CLIENT_WRONGVERSION, String.valueOf(Main.localeVersion)) + "\n");
                                writer.flush();
                                if (Main.wantColoredConsole) {
                                    System.out.println(RED + "User " + YELLOW + user.name() + RED + " tried to connect with the wrong version! " + YELLOW + "(" + GREEN + "Local: " + YELLOW + Main.localeVersion + GREEN + " | Remote: " + YELLOW + version + YELLOW + ")" + RESET);
                                } else {
                                    System.out.println("User " + user.name() + " tried to connect with the wrong version! (Local: " + Main.localeVersion + " | Remote: " + version + ")");
                                }
                            }
                        }
                    }
                } else {
                    writer.println(codec.userSendString(ServerAnswer.SOMETHING_ELSE));
                    writer.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
