package org.se13.game.grid;

/**
 * abstracted tetris grid. It contains 'Cells' to display 'Blocks'
 *
 * @author binlee0903
 */
public class TetrisGrid {
    public TetrisGrid(int rowSize, int colSize) {
        this.rowSize = rowSize;
        this.colSize = colSize;
        this.gridCells = new int[rowSize][colSize];
    }

    public void setCell(int rowIndex, int colIndex, int blockId) {
        gridCells[rowIndex][colIndex] = blockId;
    }

    public int getCell(int rowIndex, int colIndex) {
        return gridCells[rowIndex][colIndex];
    }

    /**
     * check cell's location is inside or outside
     *
     * @param rowIndex row index
     * @param colIndex column index
     * @return if cell was in, return true.
     */
    public boolean isInsideGrid(int rowIndex, int colIndex) {
        return rowIndex >= 0 && rowIndex < rowSize && colIndex >= 0 && colIndex < colSize;
    }

    /**
     * check given location's cell is empty
     *
     * @param rowIndex row index
     * @param colIndex column index
     * @return return true when cell was empty(=0)
     */
    public boolean isEmptyCell(int rowIndex, int colIndex) {
        return isInsideGrid(rowIndex, colIndex) && getCell(rowIndex, colIndex) == 0;
    }

    /**
     * check given row is full
     *
     * @param rowIndex row index
     * @return return true when row was full
     */
    public boolean isRowFull(int rowIndex) {
        int ret = 0;

        for (int i = 0; i < colSize; i++) {
            if (isEmptyCell(rowIndex, i) == false) {
                ret++;
            }
        }

        return ret == this.colSize;
    }

    public boolean isRowEmpty(int rowIndex) {
        for (int i = 0; i < colSize; i++) {
            if (getCell(rowIndex, i) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * clears full rows
     *
     * @return cleared row count
     */
    public int clearFullRows() {
        int cleared = 0;

        for (int i = rowSize - 1; i >= 0; i--) {
            if (isRowFull(i)) {
                clearRow(i);
                moveDownRows(i);
                i++;
                cleared++;
            }
        }

        return cleared;
    }

    /**
     * clear given index's row
     *
     * @param rowIndex row index
     */
    private void clearRow(int rowIndex) {
        for (int i = 0; i < colSize; i++) {
            setCell(rowIndex, i, 0);
        }
    }

    /**
     * move down row
     *
     * @param rowIndex row index
     */
    private void moveDownRows(int rowIndex) {
        for (int i = rowIndex; i >= 1; i--) {
            for (int j = 0; j < colSize; j++) {
                setCell(i, j, getCell(i - 1, j));
                setCell(i - 1, j, 0);
            }
        }
    }

    // column size of tetris board
    private final int colSize;
    // row size of tetris board
    private final int rowSize;

    /**
     * abstracted 10*22 tetris grid. originally, tetris's grid size
     * is 10*20. but I added 2 rows for block generation space.
     */
    private final int[][] gridCells;
}
