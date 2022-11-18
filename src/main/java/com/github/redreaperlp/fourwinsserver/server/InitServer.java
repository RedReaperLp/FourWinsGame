package com.github.redreaperlp.fourwinsserver.server;

import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.server.objects.Server;

import java.sql.Time;
import java.util.ArrayList;

public class InitServer {
    public static ArrayList<Integer> serverPorts = new ArrayList<Integer>();
    public static ArrayList<Server> servers = new ArrayList<Server>();

    /**
     * Initializes the server
     *
     * @param port       the port the server will listen on for new connections
     * @param maxServers the maximum amount of games that can be played at the same time
     */
    public void init(int port, int maxServers) {
        System.out.println("Starting server...");
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
