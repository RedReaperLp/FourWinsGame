package com.github.redreaperlp.fourwinsserver.server.objects;

import com.github.redreaperlp.fourwinsserver.Main;

import java.util.ArrayList;

public class Line {
    private ArrayList<Integer> row = new ArrayList<>();

    /**
     * Creates a new line
     *
     * @param width the width of the line
     */
    public Line(int width) {
        for (int i = 0; i < width; i++) {
            row.add(0);
        }
    }

    /**
     * Sets a value at a specific position in a column
     *
     * @param col   the column to set the value
     * @param value the value to set
     */
    public void setColInRow(int col, int value) {
        row.set(col, value);
    }

    /**
     * Gets a value at a specific position in a column
     *
     * @param col the column to get the value
     * @return the value at the column
     */
    public int getColInRow(int col) {
        return row.get(col);
    }

    /**
     * Gets the whole row
     *
     * @return the row
     */
    public ArrayList<Integer> getRow() {
        return row;
    }

    /**
     * @return width of the row
     */
    public int getRowSize() {
        return row.size();
    }

    /**
     * @param row the full row to set
     */
    public void setRow(ArrayList<Integer> row) {
        this.row = row;
    }

    /**
     * @return the row as a string separated by a minus(-)
     */
    public String toString() {
        String result = "";
        for (int i = 0; i < row.size(); i++) {
            if (row.size() - 1 == i) {
                result += row.get(i);
            } else {
                result += row.get(i) + "_";
            }
        }
        return result;
    }

    String RED = "\u001B[31m";
    String GREEN = "\u001B[32m";
    String RESET = "\u001B[0m";

    /**
     * @return the row as a string with "[X]" for player 1 and "[O]" for player 2 and "[ ]" for empty fields in color
     */
    public String toPrintString() {
        String result = "";
        for (int i = 0; i < row.size(); i++) {
            String num = row.get(i) + "";
            switch (num) {
                case "0":
                    result += "[ ]";
                    break;
                case "1":
                    if (Main.wantColoredConsole) {
                        result += "[" + RED + "X" + RESET + "]";
                    } else {
                        result += "[X]";
                    }
                    break;
                case "2":
                    if (Main.wantColoredConsole) {
                        result += "[" + GREEN + "O" + RESET + "]";
                    } else {
                        result += "[O]";
                    }
                    break;
            }
        }
        return result;
    }

    public Line(String sendable) {
        String[] splited = sendable.split("_");
        for (int i = 0; i < splited.length; i++) {
            row.add(Integer.parseInt(splited[i]));
        }
    }

    public boolean isFull() {
        for (int val : row) {
            if (val == 0) {
                return false;
            }
        }
        return true;
    }
}
