package com.github.redreaperlp.fourwinsserver.util;

import com.github.redreaperlp.fourwinsserver.objects.Answer;
import com.github.redreaperlp.fourwinsserver.objects.User;
import com.github.redreaperlp.fourwinsserver.objects.enums.ClientCommand;
import com.github.redreaperlp.fourwinsserver.objects.enums.ServerAnswer;

public class Codec {


    /**
     * Creates a String from a User object
     * @param user User object
     * @return String
     */
    public String userSendString(User user) {
        return userSendString(user.name(), user.password(), user.command());
    }

    /**
     * Creates a String from a User object and a String to Specify the command
     * @param user User object
     * @param specifyCommand the command to specify separated by a minus (-)
     * @return String
     */
    public String userSendString(User user, String specifyCommand) {
        return userSendString(user.name(), user.password(), user.command(), specifyCommand);
    }

    /**
     * Creates a String from a ServerAnswer enum
     * @param answer ServerAnswer enum
     * @return String with prefix "Answer:"
     */
    public String userSendString(ServerAnswer answer) {
        String finalString = "Answer:" + answer.value();
        return finalString;
    }

    /**
     * Creates a String from a ServerAnswer enum and a String to specify the command
     * @param answer ServerAnswer enum
     * @param specifyCommand the command to specify separated by a minus (-)
     * @return
     */
    public String userSendString(ServerAnswer answer, String specifyCommand) {
        String finalString = " Answer:" + answer.value() + "-" + specifyCommand; //command space seperator is "-" (minus)
        return finalString;
    }

    /**
     * Creates a String from three Strings
     * @param name Username
     * @param password Password
     * @param command ClientCommand enum
     * @return String with prefix "User:", "Password:" and "Command:"
     */
    public String userSendString(String name, String password, ClientCommand command) {
        String finalString = "Name:" + name + " Password:" + password + " Command:" + command.code(); //command space seperator is "-" (minus)
        return finalString;
    }

    /**
     * Creates a String from three Strings and a String to specify the command
     * @param name Username
     * @param password Password
     * @param command ClientCommand enum
     * @param specifyCommand the command to specify separated by a minus (-)
     * @return String with prefix "User:", "Password:" and "Command:" and the specified command at the end separated by a minus (-)
     */
    public String userSendString(String name, String password, ClientCommand command, String specifyCommand) {
        String finalString = "Name:" + name + " Password:" + password + " Command:" + command.code() + "-" + specifyCommand; //command space seperator is "-" (minus)
        return finalString;
    }

    /**
     * Creates a User object from a String
     * @param message String to decode
     * @return User object with the information from the String split by the prefix
     */
    public User toUser(String message) {
        User user = new User("", "", null);
        String[] messageArray = message.split(" ");
        for (String s : messageArray) {
            if (s.startsWith("Name:")) {
                user.setName(s.replace("Name:", ""));
            } else if (s.startsWith("Password:")) {
                user.setPassword(s.replace("Password:", ""));
            } else if (s.startsWith("Command:")) {
                String[] commandArray = s.replace("Command:", "").split("-");
                user.setCommand(ClientCommand.commandByNum(Integer.parseInt(commandArray[0])));
                if (commandArray.length > 1) {
                    user.setSpecifiedCommand(commandArray[1]);
                }
            }
        }
        return user;
    }

    /**
     * Creates a ServerAnswer enum from a String
     * @param message String to decode
     * @return Answer object with the information from the String split by the prefix
     */
    public Answer toServerAnswer(String message) {
        String[] messageArray = message.split(" ");
        for (String s : messageArray) {
            if (s.startsWith("Answer:")) {
                if (s.contains("-")) {
                    String[] answerArray = s.split("-");
                    int answerNum = Integer.parseInt(answerArray[0].replace("Answer:", ""));
                    return new Answer(ServerAnswer.answerByNum(answerNum), answerArray[1]);
                } else {
                    return new Answer(ServerAnswer.answerByNum(Integer.parseInt(s.replace("Answer:", ""))), "");
                }
            }
        }
        return null;
    }

    public SPCmd toSPCmd(String message) {
        String[] messageArray = message.split(" ");
        for (String s : messageArray) {
            if (s.startsWith("Command:")) {
                if (s.contains("-")) {
                    String[] commandArray = s.split("-");
                    int commandNum = Integer.parseInt(commandArray[0].replace("Command:", ""));
                    return new SPCmd(ClientCommand.commandByNum(commandNum), commandArray[1]);
                } else {
                    return new SPCmd(ClientCommand.commandByNum(Integer.parseInt(s.replace("Command:", ""))), "");
                }
            }
        }
        return null;
    }
}
