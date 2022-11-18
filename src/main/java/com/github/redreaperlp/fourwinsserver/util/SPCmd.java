package com.github.redreaperlp.fourwinsserver.util;

import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;

public class SPCmd {
    private ClientCommand command;
    private String specifiedCommand;

    public SPCmd(ClientCommand command, String specifiedCommand) {
        this.command = command;
        this.specifiedCommand = specifiedCommand;
    }

    public ClientCommand command() {
        return command;
    }

    public String specifiedCommand() {
        return specifiedCommand;
    }
}
