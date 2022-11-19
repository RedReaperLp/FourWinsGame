package com.github.redreaperlp.fourwinsserver.server;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.server.objects.Server;

import java.sql.Time;
import java.util.ArrayList;

public class InitServer {
    public static ArrayList<Integer> serverPorts = new ArrayList<Integer>();
    public static ArrayList<Server> servers = new ArrayList<Server>();
    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";
    String YELLOW = "\u001B[33m";
    String GREEN = "\u001B[32m";

    /**
     * Initializes the server
     *
     * @param port       the port the server will listen on for new connections
     * @param maxServers the maximum amount of games that can be played at the same time
     */
    public void init(int port, int maxServers) {
        if (Main.wantColoredConsole) {
            System.out.println(GREEN + "Starting server..." + RESET);
        } else {
            System.out.println("Starting server...");
        }
        new LandingServer(port, maxServers);
    }

    public Server getServer(int gameID) {
        for (Server server : servers) {
            if (server.gameID() == gameID) {
                return server;
            }
        }
        return null;
    }

}
