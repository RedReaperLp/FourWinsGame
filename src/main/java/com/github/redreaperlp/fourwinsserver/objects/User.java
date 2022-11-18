package com.github.redreaperlp.fourwinsserver.objects;

import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;
import com.github.redreaperlp.fourwinsserver.server.objects.Server;

public class User {
    private String name = "";
    private String password = "";
    private String specifiedCommand = "";
    private ClientCommand command;
    private boolean timedOut = false;
    private int lastPing = 0;
    private Server server;


    public User(User user) {
        this.name = user.name();
        this.password = user.password();
        this.command = user.command();
    }

    public User(String name, String password, ClientCommand command, String specifyCommand) {
        this.name = name;
        this.password = password;
        this.command = command;
        this.specifiedCommand = specifyCommand;
    }

    public User(String name, String password, ClientCommand command) {
        this.name = name;
        this.password = password;
        this.command = command;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String password() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ClientCommand command() {
        return command;
    }

    public void setCommand(ClientCommand command) {
        this.command = command;
    }

    public void pingInc() {
        this.lastPing++;
    }

    public void pingReset() {
        this.lastPing = 0;
    }

    public int lastPing() {
        return lastPing;
    }

    public void timedOut() {
        this.timedOut = true;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void notTimedOut() {
        this.timedOut = false;
    }

    public int gameID() {
        return server.gameID();
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String specifiedCommand() {
        return specifiedCommand;
    }

    public void setSpecifiedCommand(String specifiedCommand) {
        this.specifiedCommand = specifiedCommand;
    }
}
