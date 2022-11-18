package com.github.redreaperlp.fourwinsserver.server;

import com.github.redreaperlp.fourwinsserver.Main;
import com.github.redreaperlp.fourwinsserver.objects.Config;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.objects.enums.ConfigType;
import com.github.redreaperlp.fourwinsserver.server.objects.Server;
import com.github.redreaperlp.fourwinsserver.util.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Timeouter implements Runnable {
    public static ArrayList<User> users = new ArrayList<>();
    Config config = Main.config;

    @Override
    public void run() {
        int timeout = Integer.parseInt(config.getConfig(ConfigType.MAX_TIMEOUT.value()));
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
                boolean closed = false;
                for (User user : users) {
                    if (user.lastPing() >= timeout) {
                        user.timedOut();
                        System.out.println("User " + user.name() + " timed out!");
                    }
                    if (!user.isTimedOut()) {
                        user.pingInc();
                    }
                }
                List<Server> toRemove = new ArrayList<>();
                List<User> toRemoveUser = new ArrayList<>();
                boolean somethingToRemove = false;
                for (Server server : InitServer.servers) {
                    if (server.isClosed()) {
                        toRemove.add(server);
                        somethingToRemove = true;
                        InitServer.serverPorts.add(server.port());
                        server.serverThread().serverSocket().close();
                    }
                }
                for (Server server : InitServer.servers) {
                    User timedOutUser = getTimeoutedUsers(server.gameID());
                    if (timedOutUser != null) {
                        if (server.players().size() == 1 || getOnlineUsers(server.gameID()) == null) {
                            toRemove.add(server);
                            toRemoveUser.add(timedOutUser);
                            server.stop();
                            InitServer.serverPorts.add(server.port());
                            somethingToRemove = true;
                            boolean foundWon = false;
                            for (Player player : server.serverThread().players()) {
                                if (player.won()) {
                                    foundWon = true;
                                }
                            }
                            if (foundWon) {
                                System.out.println("Server " + server.gameID() + " stopped because of game end");
                            } else if (!closed) {
                                System.out.println("Server " + server.gameID() + " closed due to timeout");
                            }
                            closed = true;
                        }
                    }
                }
                if (somethingToRemove) {
                    for (Server server : toRemove) {
                        for (Player player : server.players()) {
                            User user = getByName(player.name());
                            if (user != null) {
                                users.remove(user);
                            }
                        }
                        InitServer.servers.remove(server);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(String name) {
        User user = getByName(name);
        if (user != null) {
            users.remove(user);
        }
    }

    public void resetTimeout(User user) {
        for (User u : users) {
            if (u.name().equals(user.name())) {
                u.pingReset();
                u.notTimedOut();
            }
        }
    }

    public User getByName(String name) {
        for (User user : users) {
            if (user.name().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public User getTimeoutedUsers(int gameID) {
        for (User user : users) {
            if (user.gameID() == gameID && user.isTimedOut()) {
                return user;
            }
        }
        return null;
    }

    public User getOnlineUsers(int gameID) {
        for (User user : users) {
            if (user.gameID() == gameID && !user.isTimedOut()) {
                return user;
            }
        }
        return null;
    }
}
