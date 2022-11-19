package com.github.redreaperlp.fourwinsserver;

import com.github.redreaperlp.fourwinsserver.client.Client;
import com.github.redreaperlp.fourwinsserver.objects.Config;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.server.InitServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Main {
    public static int localeVersion = 101;
    public static Config config = new Config();
    boolean isServer = false;
    public static boolean wantColoredConsole = false;
    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";
    String YELLOW = "\u001B[33m";
    String GREEN = "\u001B[32m";

    /**
     * Main method
     *
     * @param args depending on the arguments, the program will start as a server or a client
     */
    public static void main(String[] args) throws InterruptedException {
        boolean linux = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux");
        System.out.println("OS: " + System.getProperty("os.name"));
        if (linux) {
            for (int i = 0; i < 10; i++) {
                ProcessBuilder pb = new ProcessBuilder("for session in $(screen -ls | grep -o '[0-9]*\\.FourWins'); do screen -S \"${session}\" -X quit; done\n");
                try {
                    pb.start();
                    System.out.println("Killed all screens");
                } catch (IOException e) {
                }
            }
        }
        File update = new File("update.bat");
        if (update.exists()) {
            update.delete();
        }
        String[] line;
        String downloadUrl = "";
        int version = 0;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/RedReaperLP/FourWinsGame/releases/latest").openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            line = reader.readLine().split(",");
            reader.close();
            for (String check : line) {
                if (check.contains("\"browser_download_url\":")) {
                    check = check.replace("\"browser_download_url\":", "");
                    check = check.replace("\"", "");
                    check = check.replace("}", "");
                    check = check.replace("]", "");
                    downloadUrl = check;
                }
                if (check.contains("\"tag_name\":")) {
                    check = check.replace("\"tag_name\":", "");
                    check = check.replace("\"", "");
                    version = Integer.parseInt(check.replace(".", ""));
                }
            }
        } catch (IOException e) {
        }

        Main main = new Main();
        String vString = "";
        for (char c : String.valueOf(localeVersion).toCharArray()) {
            vString = vString + c + ".";
        }
        vString = vString.substring(0, vString.length() - 1);
        if (main.localeVersion >= version) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "-server":
                    main.isServer = true;
                    main.checkConfig();
                    wantColoredConsole = Boolean.parseBoolean(config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                    wantColoredConsole = Boolean.parseBoolean(Main.config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                    if (wantColoredConsole) {
                        System.out.println(main.GREEN + "FourWins " + main.RED + "v." + main.YELLOW + vString + main.GREEN + " starting as server..." + main.RESET);
                    } else {
                        System.out.println("FourWins v." + vString + " starting as server...");
                    }
                    new InitServer().init(Integer.parseInt(config.getConfig(ConfigType.SERVER_PORT.value())),
                            Integer.parseInt(config.getConfig(ConfigType.MAX_GAMES.value())));
                    break;
                case "-client":
                    main.checkConfig();
                    wantColoredConsole = Boolean.parseBoolean(config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                    if (wantColoredConsole) {
                        System.out.println(main.GREEN + "FourWins " + main.RED + "v." + main.YELLOW + vString + main.GREEN + " starting as client..." + main.RESET);
                    } else {
                        System.out.println("FourWins v." + vString + " starting as client...");
                    }
                    wantColoredConsole = Boolean.parseBoolean(Main.config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                    new Client();
                    break;
            }
        } else {
            try {
                String versionString = "";
                for (char c : String.valueOf(version).toCharArray()) {
                    versionString = versionString + c + ".";
                }
                String path = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                String fileName = path.substring(path.lastIndexOf("\\") + 1);
                TimeUnit.SECONDS.sleep(2);
                URL url = new URL(downloadUrl);
                if (!linux) {
                    System.out.println("A new version is available: " + versionString.substring(0, versionString.length() - 1) + " - Downloading now...");
                    File file = new File("update.bat");
                    FileOutputStream fos = new FileOutputStream(file);
                    DataOutputStream dos = new DataOutputStream(fos);
                    dos.writeBytes("bitsadmin.exe /transfer \"Update\" " + downloadUrl + " " + System.getProperty("user.dir") + "\\" + fileName);
                    dos.writeBytes("\njava -jar " + fileName + " " + args[0]);
                    dos.close();
                    Process process = Runtime.getRuntime().exec("cmd /c start update.bat");
                } else {
                    System.out.println("There is a new version available. Please download it from \"" + downloadUrl + "\" and restart the program.");
                }
                TimeUnit.SECONDS.sleep(10);
                System.exit(0);
            } catch (MalformedURLException e) {
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
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
