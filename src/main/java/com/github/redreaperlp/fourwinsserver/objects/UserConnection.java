package com.github.redreaperlp.fourwinsserver.objects;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class UserConnection extends User {
    private PrintWriter writer;

    private Scanner scanner;
    private int timeOut = 0;
    private int gameID = 0;

    /**
     * Constructor
     * @param socket the socket of the user
     */
    public UserConnection(Socket socket) {
        super("", "", null);
        getUserConnection(socket);

    }


    public void getUserConnection(Socket socket) {
        try {
            writer = new PrintWriter(socket.getOutputStream());
            scanner = new Scanner(socket.getInputStream());
            timeOut = 0;
            String input = scanner.nextLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int timeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int gameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public PrintWriter writer() {
        return writer;
    }

    public Scanner scanner() {
        return scanner;
    }
}
