package com.github.redreaperlp.fourwinsserver;

import com.github.redreaperlp.fourwinsserver.client.Client;
import com.github.redreaperlp.fourwinsserver.objects.Config;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.server.InitServer;

import java.io.Console;
import java.util.Locale;

public class Main {
    public static Config config = new Config();
    boolean isServer = false;
    public static boolean wantColoredConsole = false;


    /**
     * Main method
     *
     * @param args depending on the arguments, the program will start as a server or a client
     */
    public static void main(String[] args) {
        Main main = new Main();
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "-server":
                main.isServer = true;
                main.checkConfig();
                wantColoredConsole = Boolean.parseBoolean(Main.config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                new InitServer().init(Integer.parseInt(config.getConfig(ConfigType.SERVER_PORT.value())),
                        Integer.parseInt(config.getConfig(ConfigType.MAX_GAMES.value())));
                break;
            case "-client":
                main.checkConfig();
                wantColoredConsole = Boolean.parseBoolean(Main.config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                new Client();
                break;
        }
    }

    public void reInitClient() {
        new Client();
    }

    /**
     * Checks if the config file exists and creates it if it doesn't
     * Asks the user for some information if the config file is empty
     */
    public void checkConfig() {
        if (!isServer) {
            if (config.getConfig(ConfigType.PLAYER_NAME.value()) == null) {
                System.out.println("Please insert your Username");
                String s = consoleInput(false);
                config.setConfig(ConfigType.PLAYER_NAME.value(), s);
            }
            if (config.getConfig(ConfigType.PLAYER_PASSWORD.value()) == null) {
                System.out.println("Please insert your Password");
                Console console = System.console();
                config.setConfig(ConfigType.PLAYER_PASSWORD.value(), new String(console.readPassword()));
            }
            if (config.getConfig(ConfigType.SERVER_ADDRESS.value()) == null) {

                System.out.println("Please insert the Server address");
                String s = consoleInput(false);
                config.setConfig(ConfigType.SERVER_ADDRESS.value(), s);
            }
        }
        if (config.getConfig(ConfigType.SERVER_PORT.value()) == null) {
            System.out.println("Please insert the Server port");
            String s = consoleInput(true);
            config.setConfig(ConfigType.SERVER_PORT.value(), s);
        }

        if (config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()) == null) {
            System.out.println("Do you want to use colored console? (y/n)");
            String s = consoleInput(false);
            boolean isTrueOrFalse = false;
            do {
                if (s.equalsIgnoreCase("y")) {
                    config.setConfig(ConfigType.WANT_COLORED_CONSOLE.value(), "true");
                    isTrueOrFalse = true;
                } else if (s.equalsIgnoreCase("n")) {
                    config.setConfig(ConfigType.WANT_COLORED_CONSOLE.value(), "false");
                    isTrueOrFalse = true;
                } else {
                    System.out.println("Please insert y or n");
                    s = consoleInput(false);
                }
            } while (!isTrueOrFalse);
        }
        if (isServer) {
            if (config.getConfig(ConfigType.MAX_TIMEOUT.value()) == null) {
                System.out.println("Please insert max Timeout");
                String s = consoleInput(true);
                config.setConfig(ConfigType.MAX_TIMEOUT.value(), s);
            }

            if (config.getConfig(ConfigType.MAX_GAMES.value()) == null) {
                System.out.println("Please insert max Games");
                String s = consoleInput(true);
                config.setConfig(ConfigType.MAX_GAMES.value(), s);
            }

            if (config.getConfig(ConfigType.FIELD_HEIGHT.value()) == null) {
                System.out.println("Please insert the Field height");
                String s = consoleInput(true);
                config.setConfig(ConfigType.FIELD_HEIGHT.value(), s);
            }

            if (config.getConfig(ConfigType.FIELD_WIDTH.value()) == null) {
                System.out.println("Please insert the Field width");
                String s = consoleInput(true);
                config.setConfig(ConfigType.FIELD_WIDTH.value(), s);
            }
        }
        config.saveConfig();
    }

    /**
     * Reads the input from the console
     *
     * @param isInt if the input should be a number to check for a valid number if needed
     * @return the input
     */
    public String consoleInput(boolean isInt) {
        Console console = System.console();
        String input = console.readLine();
        if (isInt) {
            try {
                Integer.parseInt(input);
            } catch (Exception e) {
                System.out.print("Invalid input! Try again: ");
                return consoleInput(isInt);
            }
        }
        return input;
    }
}
