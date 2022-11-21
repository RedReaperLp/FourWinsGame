package com.github.redreaperlp.fourwinsserver.util;

public class Player {
    private String name;
    private boolean isMyTurn;
    private int playerSign;
    private boolean won;
    private boolean reconnected;

    public Player(String name, boolean isMyTurn, int playerSign) {
        this.name = name;
        this.isMyTurn = isMyTurn;
        this.playerSign = playerSign;
    }
    public String name() {
        return name;
    }
    public boolean isMyTurn() {
        return isMyTurn;
    }
    public void changeTurn() {
        isMyTurn = !isMyTurn;
    }
    public int sign() {
        return playerSign;
    }

    public boolean won() {
        return won;
    }

    public void won(boolean won) {
        this.won = won;
    }

    public boolean reconnected() {
        return reconnected;
    }

    public void reconnected(boolean reconnected) {
        this.reconnected = reconnected;
    }
}
