package com.example.dan_p.hw2.minesweeper;

import java.util.LinkedList;

public class GameLogic {
    private int rows;
    private int columns;
    private Cell[][] gameGrid;
    private int numberOfMines;

    private int numberOfFlags;
    private int numberOfCellsRevealed;
    private GameState gameState;

    private Thread timer;
    private boolean runTimer;
    private int time;

    public GameLogic(DifficultyLevel difficultyLevel) {
        this(difficultyLevel.getRows(), difficultyLevel.getColumns(),
                difficultyLevel.getNumberOfMines());
    }

    public GameLogic(int rows, int columns, int numberOfMines){
        this.rows = rows;
        this.columns = columns;


        if (numberOfMines < this.rows * this.columns)
            this.numberOfMines = numberOfMines;
        else
            this.numberOfMines = (this.rows * this.columns) - 1;

        this.gameGrid = new Cell[this.rows][this.columns];

        this.numberOfFlags = 0;
        this.numberOfCellsRevealed = 0;
        createNewBoard();
        this.gameState = GameState.STARTING;

        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    time = 0;
                    while(gameState != GameState.WON || gameState != GameState.LOST){
                        timer.sleep(1000);
                        if (GameLogic.this.runTimer)
                            time++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createNewBoard() {
        for ( int r = 0 ; r < this.rows ; r++ )
            for (int c = 0 ; c < this.columns ; c++ )
                this.gameGrid[r][c] = new Cell(r, c);
    }

    private boolean isInBounds(int row, int column) {
        return (row >= 0 && row < this.rows) && (column >= 0 && column < this.columns);
    }

    private void placeMines(int firstRow, int firstColumn) {
        int numberOfMines = this.numberOfMines;
        int position, row, column;

        while (numberOfMines != 0) {
            position = (int)Math.abs(Math.random() * (this.rows * this.columns));
            row = position / this.rows;
            column = position % this.rows;

            if (isInBounds(row, column) && (row != firstRow || column != firstColumn)
                    && !this.gameGrid[row][column].isMine()) {
                this.gameGrid[row][column].setMine(true);
                numberOfMines--;

                for (int r = row -1 ; r <= row + 1 ; r++ ) {
                    for (int c = column -1 ; c <= column + 1 ; c++ ) {
                        // For each cell that isn't out of bounds and isn't the current cell
                        if (isInBounds(r, c) && (r != row || c != column)
                                && !this.gameGrid[r][c].isMine())
                            gameGrid[r][c].increaseValue();
                    }
                }
            }
        }
    }

    public GameState makeMove(int row, int column) {
        boolean notMine = true;
        if (isInBounds(row, column)&& !this.gameGrid[row][column].isFlagged()) {
            if (this.gameState == GameState.STARTING) {
                placeMines(row, column);
                notMine = revealCells(row, column);
                this.gameState = GameState.IN_PROGRESS;
                this.setRunTimer(true);
                this.timer.start();
            }
            else if (this.gameState == GameState.IN_PROGRESS) {
                notMine = revealCells(row, column);
            }
        }

        if (!notMine) {
            setRunTimer(false);
            this.gameState = GameState.LOST;
            revealAllMines();
        }
        else if (checkWin()) {
            setRunTimer(false);
            this.gameState = GameState.WON;
        }

        return this.gameState;
    }

    public void flag(int row, int column) {
        if ((this.gameState == GameState.IN_PROGRESS || this.gameState == GameState.STARTING)
                && isInBounds(row, column) && !this.gameGrid[row][column].isRevealed()) {
            setRunTimer(true);
            Cell cell = this.gameGrid[row][column];
            if (cell.isFlagged()) {
                cell.setIsFlagged(false);
                this.numberOfFlags--;
            }
            else {
                cell.setIsFlagged(true);
                this.numberOfFlags++;
            }
        }
    }

    private boolean revealCells(int row, int column) {
        Cell cell = this.gameGrid[row][column];
        if (cell.isMine()){
            cell.setRevealed(true);
            return false; // Game lost
        }

        LinkedList<Cell> cellRevealQueue = new LinkedList<>();
        cellRevealQueue.addFirst(cell);

        while (!cellRevealQueue.isEmpty()) {
            cell = cellRevealQueue.removeLast();
            cell.setRevealed(true);
            numberOfCellsRevealed++;
            if (cell.getValue() == 0 ) {
                for (int r = cell.getRow() -1 ; r <= cell.getRow()  + 1 ; r++ ) {
                    for (int c = cell.getColumn() - 1; c <= cell.getColumn() + 1; c++) {
                        if (isInBounds(r, c)) {
                            Cell adjacentCell = this.gameGrid[r][c];
                            if (!adjacentCell.isRevealed()
                                    && !cellRevealQueue.contains(adjacentCell)
                                    && !this.gameGrid[r][c].isFlagged())
                                cellRevealQueue.addFirst(adjacentCell);
                        }
                    }
                }
            }
        }

        return true; // The game is still in progress
    }

    private void revealAllMines() {
        for (int row = 0 ; row < getRows() ; row++ ) {
            for (int column = 0 ; column < getColumns() ; column++ )
                if (gameGrid[row][column].isMine())
                    gameGrid[row][column].setRevealed(true);
        }
    }

    private boolean checkWin() {
        int numberOfCells = this.getRows() * this.getColumns();
        return getNumberOfCellsRevealed() == (numberOfCells - getNumberOfMines());
    }

    public void addMine() {
        boolean isMinePlaced = false;
        int position, row, column;

        if (this.gameState == GameState.IN_PROGRESS && getNumberOfMines() != getRows() * getColumns())
            while (!isMinePlaced) {
                position = (int)Math.abs(Math.random() * (this.rows * this.columns));
                row = position / this.rows;
                column = position % this.rows;
                Cell cell = this.gameGrid[row][column];
                if (isInBounds(cell.getRow(), cell.getColumn()) && !cell.isMine()) {
                    cell.setMine(true);

                    if (cell.isRevealed()) {
                         cell.setRevealed(false);
                         this.numberOfCellsRevealed--;
                    }

                    this.numberOfMines++;

                    isMinePlaced = true;

                    for (int r = cell.getRow() - 1; r <= cell.getRow() + 1; r++)
                        for (int c = cell.getColumn() - 1; c <= cell.getColumn() + 1; c++)
                            // For each cell that isn't out of bounds and isn't the current cell
                            if (isInBounds(r, c) && (r != cell.getRow() || c != cell.getColumn())
                                    && !this.gameGrid[r][c].isMine())
                                gameGrid[r][c].increaseValue();


                }
            }
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public int getNumberOfCellsRevealed() {
        return numberOfCellsRevealed;
    }

    public void setNumberOfCellsRevealed(int numberOfCellsRevealed) {
        this.numberOfCellsRevealed = numberOfCellsRevealed;
    }

    public int getNumberOfMines() {
        return numberOfMines;
    }

    public Cell[][] getGameGrid() {
        return gameGrid;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int numberOfMinesLeft() {
        int numberOfMinesLeft = this.numberOfMines - this.numberOfFlags;
        if (numberOfMinesLeft >= 0)
            return numberOfMinesLeft;
        else
            return 0;
    }

    public int getTime() {
        return time;
    }

    public void setRunTimer(boolean runTimer) {
        if (this.gameState == GameState.IN_PROGRESS)
            this.runTimer = runTimer;
        else
            this.runTimer = false;
    }

    public class Cell {
        private int row, column;
        private int value = 0;
        private boolean isMine = false;
        private boolean isRevealed = false;
        private boolean isFlagged = false;

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public boolean isFlagged() {
            return this.isFlagged;
        }

        public void setIsFlagged(boolean isFlagged) {
            this.isFlagged = isFlagged;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public boolean isRevealed() {
            return isRevealed;
        }

        public void setRevealed(boolean revealed) {
            isRevealed = revealed;
        }

        public int getValue() {
            return this.value;
        }

        public void increaseValue() {
            if (this.getValue() != 9)
                value++;
        }

        public boolean isMine() {
            return isMine;
        }

        public void setMine(boolean mine) {
            isMine = mine;
        }

        public String toString() {
            if (isMine())
                return "Mine";
            else
                return getValue()+" "+isRevealed();
        }
    }
}
