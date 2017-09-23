/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.flavour.minesweeper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import org.teavm.flavour.templates.BindTemplate;
import org.teavm.flavour.templates.Templates;

@BindTemplate("templates/minesweeper.html")
public class MinesweeperGame {
    private Cell[][] cells;
    private int rows;
    private int columns;
    private GameState state;
    private int emptyLeft;

    public MinesweeperGame(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        restart();
    }

    public void restart() {
        state = GameState.IN_PROGRESS;
        this.cells = new Cell[rows][columns];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                cells[i][j] = new Cell();
            }
        }
        initMines();
        calculateMinesAround();
    }

    public GameState getState() {
        return state;
    }

    private void initMines() {
        Random random = new Random();
        int mines = rows * columns / 7;
        emptyLeft = rows * columns - mines;
        for (int i = 0; i < mines; ++i) {
            int row = random.nextInt(rows);
            int column = random.nextInt(columns);
            if (cells[row][column].mine) {
                --i;
            } else {
                cells[row][column].mine = true;
            }
        }
    }

    private void calculateMinesAround() {
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                int count = 0;
                for (Coordinates coord : cellsAround(i, j)) {
                    if (cells[coord.row][coord.column].mine) {
                        ++count;
                    }
                }
                cells[i][j].minesAround = count;
            }
        }
    }

    private List<Coordinates> cellsAround(int i, int j) {
        List<Coordinates> filtered = new ArrayList<>();
        for (Coordinates coord : allCellsAround(i, j)) {
            if (coord.column >= 0 && coord.row >= 0 && coord.column < columns && coord.row < rows) {
                filtered.add(coord);
            }
        }
        return filtered;
    }

    private List<Coordinates> allCellsAround(int i, int j) {
        List<Coordinates> list = new ArrayList<>();
        list.add(new Coordinates(i - 1, j - 1));
        list.add(new Coordinates(i - 1, j));
        list.add(new Coordinates(i - 1, j + 1));
        list.add(new Coordinates(i, j - 1));
        list.add(new Coordinates(i, j + 1));
        list.add(new Coordinates(i + 1, j - 1));
        list.add(new Coordinates(i + 1, j));
        list.add(new Coordinates(i + 1, j + 1));
        return list;
    }

    public Cell cellAt(int row, int column) {
        return cells[row][column];
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public void open(int row, int column) {
        if (state != GameState.IN_PROGRESS) {
            return;
        }
        if (cellAt(row, column).mine) {
            lose();
            return;
        }

        Queue<Coordinates> worklist = new ArrayDeque<>();
        worklist.add(new Coordinates(row, column));
        while (!worklist.isEmpty()) {
            Coordinates coord = worklist.remove();
            Cell cell = cells[coord.row][coord.column];
            if (cell.getState() != CellState.UNKNOWN) {
                continue;
            }
            emptyLeft--;
            cell.state = CellState.OPEN;
            if (cell.minesAround == 0) {
                worklist.addAll(cellsAround(coord.row, coord.column));
            }
        }

        if (emptyLeft == 0) {
            win();
        }
    }

    private void lose() {
        state = GameState.LOST;
        showMines();
    }

    private void win() {
        state = GameState.WON;
        showMines();
    }

    private void showMines() {
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                Cell cell = cells[i][j];
                if (cell.mine) {
                    cell.state = CellState.MINE;
                }
            }
        }
    }

    static class Coordinates {
        int row;
        int column;
        public Coordinates(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    public static void main(String[] args) {
        Templates.bind(new MinesweeperGame(15, 15), "minefield");
    }
}
