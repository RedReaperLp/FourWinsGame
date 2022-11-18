package com.github.redreaperlp.fourwinsserver.server.objects;

import java.util.ArrayList;
import java.util.List;

public class LineBlock {
    private ArrayList<Line> lines = new ArrayList<>();

    /**
     * Constructor for the GameField
     *
     * @param height the height of the GameField
     * @param width  the width of the GameField
     */
    public LineBlock(int height, int width) {
        for (int i = 0; i < height; i++) {
            lines.add(new Line(width));
        }
    }

    /**
     * Sets a value in the GameField if the position is free
     *
     * @param row   the row of the GameField
     * @param value the value to set
     * @return true if the value was set, false if not (e.g. if the row is already full)
     */
    public boolean[] setRowValue(int row, int value) {
        boolean[] result = new boolean[2];
        for (int i = lines.size() - 1; i >= 0; i--) {
            Line line = lines.get(i);
            if (line.getColInRow(row) == 0) {
                line.setColInRow(row, value);
                boolean won = false;
                //TODO: Check if the game is won
                result[0] = true;
                won = checkWin(i, row, value);
                result[1] = won;
                return result;
            }
        }
        return result;
    }

    /**
     * prints the GameField, first line with number of the columns and then the rows
     */
    public void print() {
        String topNumberPrint = "";
        for (int i = 1; i < lines.get(0).getRowSize() + 1; i++) {
            if (i < 10) {
                topNumberPrint += " " + i + " ";
            } else {
                topNumberPrint += i + " ";
            }
        }
        System.out.println(topNumberPrint);
        for (int i = 0; i < lines.size(); i++) {
            System.out.println(lines.get(i).toPrintString());
        }
    }

    public String toSendable() {
        String sendable = "";
        for (int i = 0; i <= lines.size() - 1; i++) {
            if (i != lines.size() - 1) {
                sendable += lines.get(i).toString() + "/";
            } else {
                sendable += lines.get(i).toString();
            }
        }
        return sendable;
    }

    public LineBlock(String sendable) {
        String[] splits = sendable.split("/");
        for (int i = 0; i < splits.length; i++) {
            Line l = new Line(splits[i]);
            lines.add(l);
        }
    }

    public int getRowSize() {
        return lines.get(0).getRowSize();
    }

    public boolean checkWin(int col, int row, int val) {
        String check = "";
        //Check horizontal
        {
            Line line = lines.get(col);
            for (int i : line.getRow()) {
                check += i + "-";
            }
            if (check.contains(val + "-" + val + "-" + val + "-" + val)) {
                System.out.println(check);
                return true;
            }
        }

        //Check vertical
        {
            check = "";
            for (int i = 0; i < lines.size(); i++) {
                check += lines.get(i).getColInRow(row) + "-";
            }
            if (check.contains(val + "-" + val + "-" + val + "-" + val)) {
                System.out.println(check);
                return true;
            }
        }

        //Check diagonal
        {
            check = "";
            int x = col;
            int y = row;
            while (x > 0 && y > 0) {
                x--;
                y--;
            }
            while (x < lines.size() && y < lines.get(0).getRowSize()) {
                check += lines.get(x).getColInRow(y) + "-";
                x++;
                y++;
            }
            if (check.contains(val + "-" + val + "-" + val + "-" + val)) {
                System.out.println(check);
                return true;
            }
        }

        //Check diagonal
        {
            check = "";
            int x = col;
            int y = row;
            while (x > 0 && y < lines.get(0).getRowSize() - 1) {
                x--;
                y++;
            }
            while (x < lines.size() && y >= 0) {
                check += lines.get(x).getColInRow(y) + "-";
                x++;
                y--;
            }
            if (check.contains(val + "-" + val + "-" + val + "-" + val)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        for (Line line : lines) {
            if (!line.isFull()) {
                return false;
            }
        }
        return true;
    }
}
