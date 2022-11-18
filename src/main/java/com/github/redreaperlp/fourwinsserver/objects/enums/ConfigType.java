package com.github.redreaperlp.fourwinsserver.objects.enums;

public enum ConfigType {
    SERVER_PORT("SERVER_Port", true),
    SERVER_ADDRESS("SERVER_Address", false),
    MAX_TIMEOUT("MAX_Timeout", true),
    MAX_GAMES("MAX_Games", true),
    PLAYER_NAME("PLAYER_Name", false),
    PLAYER_PASSWORD("PLAYER_Password", false),
    FIELD_HEIGHT("FIELD_Height", true),
    FIELD_WIDTH("FIELD_Width", true),
    WANT_COLORED_CONSOLE("Want_Colored_Console", true);

    private String pathValue;
    private boolean availableAsServer;

    ConfigType(String pathValue, boolean availableAsServer) {
        this.pathValue = pathValue;
        this.availableAsServer = availableAsServer;
    }

    public String value() {
        return pathValue;
    }

    public boolean available() {
        return availableAsServer;
    }
}
