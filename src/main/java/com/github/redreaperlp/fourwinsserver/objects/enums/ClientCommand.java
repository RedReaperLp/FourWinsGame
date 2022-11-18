package com.github.redreaperlp.fourwinsserver.objects.enums;

public enum ClientCommand {
    PING(1),
    ASK_FOR_SERVER(2),
    SET_STONE(10),
    NO_SUCH_COMMAND(404);

    private int code;
    ClientCommand(int code) {
        this.code = code;
    }

    ClientCommand(){}

    public int code() {
        return code;
    }

    public static ClientCommand commandByNum(int i) {
        for (ClientCommand cmd : ClientCommand.values()) {
            if (cmd.code == i) {
                return cmd;
            }
        }
        return NO_SUCH_COMMAND;
    }
}
