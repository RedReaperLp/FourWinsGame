package com.github.redreaperlp.fourwinsserver.server.objects;

import com.github.redreaperlp.fourwinsserver.server.Timeouter;
import com.github.redreaperlp.fourwinsserver.util.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Time;
import java.util.ArrayList;

public class Server {
    private int port;
    private int gameID;


    private boolean isClosed = false;
    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private Thread thread;
    /**
     * Constructor for creating a new Serversocket and a new ServerThread
     * @param port the port the server should listen to
     * @param gameID the gameID the server uses
     */
    public Server(int port, int gameID) {
        this.port = port;
        this.gameID = gameID;
        try {
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket, gameID);
            thread = new Thread(serverThread);
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return the port the server is listening to
     */
    public int port() {
        return port;
    }

    /**
     *
     * @return the gameID the server uses
     */
    public int gameID() {
        return gameID;
    }

    /**
     *
     * @param name the name of the player
     * @return true if the player is in playerList
     */
    public boolean containsPlayer(String name) {
        return serverThread.containsPlayer(name);
    }

    public ArrayList<Player> players() {
        return serverThread.players();
    }

    /**
     * adds a player to the playerList
     * @param player the player to add
     */
    public void addPlayer(Player player) {
        serverThread.addPlayer(player);
    }

    /**
     * gets the size of the playerList
     * @return
     */
    public int size() {
        return serverThread.size();
    }

    public ServerThread serverThread() {
        return serverThread;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        isClosed = true;
    }
    public void stop() {
        try {
            thread.interrupt();
            serverThread.serverSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
