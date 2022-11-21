package com.github.redreaperlp.fourwinsserver;

import com.github.redreaperlp.fourwinsserver.client.Client;
import com.github.redreaperlp.fourwinsserver.objects.Config;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.server.InitServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Main {
    public static int localeVersion = 111;
    public int latestVersion = 0;
    public String versionString = "";
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
        Main main = new Main();
        Console console = System.console();
        boolean linux = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux");
        File startFileWindows = new File("start.bat");
        File startFileLinux = new File("start.sh");
        if (console == null) {
            if (linux) {
                createRunFile(startFileLinux);
            } else {
                createRunFile(startFileWindows);
            }
        }
        if (linux) {
            main.createStartLinux(args, startFileLinux);
        } else {
            main.createStartWindows(args, startFileWindows);
        } //Checks if there is a start file and if there is none, it creates one for Linux or Windows
        File update = new File("update.bat");
        if (update.exists()) {
            update.delete();
        }
        String[] line;
        String downloadUrl = "";
        int version = 0;
        boolean errorCheckUrl = false;
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
                } //check for download url
                if (check.contains("\"tag_name\":")) {
                    check = check.replace("\"tag_name\":", "");
                    check = check.replace("\"", "");
                    version = Integer.parseInt(check.replace(".", ""));
                } //check for version string
            }
        } catch (IOException e) {
            if (wantColoredConsole) {
                System.out.println(main.RED + "Error while checking for Updates, make sure to have a wifi connection");
            } else {
                System.out.println("Error while checking for Updates, make sure to have a wifi connection");
            }
            errorCheckUrl = true;
        }
        String vString = "";
        for (char c : String.valueOf(localeVersion).toCharArray()) {
            vString = vString + c + ".";
        } //add dots to version string
        vString = vString.substring(0, vString.length() - 1); //remove last dot
        if (main.localeVersion >= version || errorCheckUrl) {
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
                    } //Colored if wanted
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
                    } //Colored if wanted
                    wantColoredConsole = Boolean.parseBoolean(Main.config.getConfig(ConfigType.WANT_COLORED_CONSOLE.value()));
                    new Client();
                    break;
            } //check for server or client
        } else {
            try {
                main.versionString = "";
                for (char c : String.valueOf(version).toCharArray()) {
                    main.versionString = main.versionString + c + ".";
                }
                String path = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                String fileName = path.substring(path.lastIndexOf("\\") + 1);
                URL url = new URL(downloadUrl);
                if (!linux) {
                    main.updateWindows(fileName, downloadUrl, args);
                } else {
                    main.updateLinux(fileName, downloadUrl, args);
                }
                TimeUnit.SECONDS.sleep(1);
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //check for a newer version
    }

    public void updateWindows(String fileName, String downloadUrl, String[] args) {
        try {
            System.out.println("A new version is available: " + versionString.substring(0, versionString.length() - 1) + " - Downloading now...");
            File file = new File("update.bat");
            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeBytes("bitsadmin.exe /transfer \"Update\" " + downloadUrl + " \"" + System.getProperty("user.dir") + "\\" + fileName+"\"");
            dos.writeBytes("\nstart start.bat");
            dos.writeBytes("\nexit");
            dos.close();
            TimeUnit.SECONDS.sleep(2);
            Process process = Runtime.getRuntime().exec("cmd /c start update.bat");
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } //Starts the update Linux
    }

    public void updateLinux(String fileName, String downloadUrl, String[] args) {
        try {
            System.out.println();
            Process process = Runtime.getRuntime().exec("wget " + downloadUrl + " -O " + fileName);
            process.waitFor();
            System.out.println(versionString.substring(0, versionString.length() - 1) + " downloaded, please restart the program to apply the update.");
            TimeUnit.SECONDS.sleep(2);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } //Starts the update for windows
    }

    public void createStartLinux(String[] args, File startFile) {
        Main main = new Main();
        if (args.length == 0) {
            System.out.println("Please insert the arguments \"[1]\" or \"[2]\" to start the program as a [server] or a [client]");
            main.consoleInput();
            try {
                if (main.isServer) {
                    startFile.createNewFile();
                    FileWriter writer = new FileWriter(startFile);
                    writer.write("screen -S \"FourWins\" java -jar FourWins.jar -server");
                    writer.close();
                    ProcessBuilder pb = new ProcessBuilder("chmod", "+x", "start.sh");
                    pb.start();
                } else {
                    startFile.createNewFile();
                    FileWriter writer = new FileWriter(startFile);
                    writer.write("java -jar FourWins.jar -client");
                    writer.close();
                }
                System.out.println("Please restart the program, you can now start it with \"./start.sh\"");
                TimeUnit.SECONDS.sleep(5);
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //Asks for the start arguments "server" or "client" if none are given and updates the start.sh file
    }

    public void createStartWindows(String[] args, File startFile) {
        Main main = new Main();
        if (args.length == 0) {
            System.out.println("Please insert the arguments \"[1]\" or \"[2]\" to start the program as a [server] or a [client]");
            main.consoleInput();
            try {
                String path = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                path = path.substring(path.lastIndexOf("\\") + 1);
                if (main.isServer) {
                    startFile.createNewFile();
                    FileWriter writer = new FileWriter(startFile);
                    writer.write("java -jar " + path + " -server");
                    writer.close();
                } else {
                    startFile.createNewFile();
                    FileWriter writer = new FileWriter(startFile);
                    writer.write("java -jar " + path + " -client");
                    writer.close();
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } //Asks for the start arguments "server" or "client" if none are given and updates the start.bat file
    }


    private static void createRunFile(File startFile) {
        if (!startFile.exists()) {
            try {
                startFile.createNewFile();
                FileWriter writer = new FileWriter(startFile);
                String path = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                writer.write("java -jar " + path.substring(path.lastIndexOf("\\") + 1));
                writer.close();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.exit(0);
        } //Creates the run file
    }

    public void reInitClient() {
        new Client();
    }

    public void consoleInput() {
        Console console = System.console();
        String input = console.readLine();
        if (input.equals("1") || input.equals("server")) {
            isServer = true;
        } else if (input.equals("2") || input.equals("client")) {
            isServer = false;
        } else {
            System.out.println("Please enter a valid Input!");
            consoleInput();
        }
    }

    /**
     * Checks if the config file exists and creates it if it doesn't
     * Ask the user for some information if the config file is empty
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
