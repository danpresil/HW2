package com.example.dan_p.hw2.minesweeper;

public enum DifficultyLevel {

    EASY(10, 10, 5),
    NORMAL(10, 10, 10),
    HARD(5, 5, 10);

    private final int rows;
    private final int columns;
    private final int numberOfMines;

    private DifficultyLevel(int rows, int columns, int numberOfMines) {
        this.rows = rows;
        this.columns = columns;
        this.numberOfMines = numberOfMines;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getNumberOfMines() {
        return numberOfMines;
    }

}
