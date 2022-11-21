package com.github.redreaperlp.fourwinsserver.objects.enums;

public enum ServerAnswer {
    NO_SUCH_COMMAND(1),
    NOT_YOUR_TURN(2),
    YOUR_TURN(3),
    GAME_FULL(4),
    COL_FULL(5),
    GAME_NOT_FOUND(6),
    NOTIFY_CLIENT_GAMESTART(10),
    NOTIFY_CLIENT_YOURTURN(11),
    NOTIFY_CLIENT_CHANGEPORT(12),
    NOTIFY_CLIENT_TIMEDOUT(13),
    NOTIFY_CLIENT_RECONNECT(14),
    NOTIFY_CLIENT_GAMESTONESET(15),
    NOTIFY_CLIENT_INVALIDSET(16),
    NOTIFY_CLIENT_WON(17)   ,
    NOTIFY_CLIENT_OPPONENTWON(18),
    NOTIFY_CLIENT_WRONGVERSION(19),
    ALREADY_LOGGED_IN(99),
    NO_GAMES_AVAILABLE(7),
    WRONG_PASSWORD(30),
    WAITING_FOR_PLAYER(17),
    JOIN_NOT_ALLOWED(20),
    SOMETHING_ELSE(21),
    FIELD_FULL(22);
    private int value;

    ServerAnswer(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static ServerAnswer answerByNum(int i) {
        for (ServerAnswer answer : ServerAnswer.values()) {
            if (answer.value == i) {
                return answer;
            }
        }
        return NO_SUCH_COMMAND;
    }
}


