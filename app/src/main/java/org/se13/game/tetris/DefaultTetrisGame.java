package org.se13.game.tetris;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.se13.game.timer.BlockCollideTimer;
import org.se13.game.timer.Timer;
import org.se13.game.block.Block;
import org.se13.game.block.BlockPosition;
import org.se13.game.block.CurrentBlock;
import org.se13.game.config.InputConfig;
import org.se13.game.grid.TetrisGrid;
import org.se13.game.input.InputManager;
import org.se13.game.rule.BlockQueue;
import org.se13.sqlite.config.ConfigRepositoryImpl;

import java.util.Random;

public class DefaultTetrisGame implements ITetrisGame {
    enum GameStatus {
        GAMEOVER,
        RUNNING,
        PAUSED
    }

    public DefaultTetrisGame(Canvas tetrisGameCanvas, Canvas nextBlockCanvas, Label scoreLabel) {
        this.blockQueue = new BlockQueue(new Random().nextInt());
        this.tetrisGameGrid = new TetrisGrid(ROW_SIZE, COL_SIZE);
        this.gameGraphicsContext = tetrisGameCanvas.getGraphicsContext2D();
        this.nextBlockGraphicsContext = nextBlockCanvas.getGraphicsContext2D();
        this.scoreLabel = scoreLabel;

        this.gameStatus = GameStatus.PAUSED;
        this.score = 0;
        this.CANVAS_WIDTH = tetrisGameCanvas.getWidth();
        this.CANVAS_HEIGHT = tetrisGameCanvas.getHeight();

        this.isGameStarted = false;
        this.isBlockPlaced = false;
        this.isBlockCollided = false;

        this.currentBlock = nextBlock();
        this.nextBlock = nextBlock();

        this.configRepository = new ConfigRepositoryImpl();
        this.configRepository.createNewTableConfig();
        this.configRepository.insertDefaultConfig(0);

        this.inputManager = InputManager.getInstance(scoreLabel.getScene());
        this.inputConfig = new InputConfig(this.configRepository);

        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (gameStatus == GameStatus.RUNNING) {
                    if (inputManager.peekInput()) {
                        processUserInput(inputManager.getInput());
                    }

                    if (isGameStarted == false) {
                        blockMovingTimer = new Timer(l);
                        collideCheckingTimer = new BlockCollideTimer(l);
                        isGameStarted = true;
                        drawNextBlock();
                    }

                    tick(l);
                    prepare();
                    render();
                    update();
                } else if (gameStatus == GameStatus.PAUSED) {
                    animationTimer.stop();
                } else {
                    animationTimer.stop();
                }
            }
        };
    }

    @Override
    public void startGame() {
        this.gameStatus = GameStatus.RUNNING;

        animationTimer.start();
    }

    @Override
    public void stopGame() {
        this.gameStatus = GameStatus.GAMEOVER;
    }

    @Override
    public void pauseGame() {
        this.gameStatus = GameStatus.PAUSED;
    }

    @Override
    public int getScore() {
        return this.score;
    }

    private CurrentBlock nextBlock() {
        return new CurrentBlock(blockQueue.nextBlock());
    }

    private boolean blockFits() {
        for (BlockPosition p : currentBlock.shape()) {
            int blockRowIndex = p.getRowIndex() + currentBlock.getPosition().getRowIndex();
            int blockColIndex = p.getColIndex() + currentBlock.getPosition().getColIndex();

            if (tetrisGameGrid.isEmptyCell(blockRowIndex, blockColIndex) == false) {
                return false;
            }

            if (tetrisGameGrid.isInsideGrid(blockRowIndex, blockColIndex) == false) {
                return false;
            }
        }

        return true;
    }

    private void rotateBlockCW() {
        deleteCurrentBlockFromGrid();
        currentBlock.rotateCW();

        if (blockFits() == false) {
            currentBlock.rotateCCW();
        }
    }

    private void rotateBlockCCW() {
        deleteCurrentBlockFromGrid();
        currentBlock.rotateCCW();

        if (blockFits() == false) {
            currentBlock.rotateCW();
        }
    }

    private void moveBlockLeft() {
        deleteCurrentBlockFromGrid();
        currentBlock.move(0, -1);

        if (blockFits() == false) {
            currentBlock.move(0, 1);
        }
    }

    private void moveBlockRight() {
        deleteCurrentBlockFromGrid();
        currentBlock.move(0, 1);

        if (blockFits() == false) {
            currentBlock.move(0, -1);
        }
    }

    private void moveBlockDown() {
        deleteCurrentBlockFromGrid();
        currentBlock.move(1, 0);
        this.score++;

        if (blockFits() == false) {
            isBlockCollided = true;
            this.score--;
            currentBlock.move(-1, 0);
        } else {
            isBlockCollided = false;
        }
    }

    private void immediateBlockPlace() {
        deleteCurrentBlockFromGrid();

        for (int i = currentBlock.getPosition().getRowIndex(); i < ROW_SIZE; i++) {
            currentBlock.move(1, 0);
            if (blockFits() == false) {
                isBlockPlaced = true;
                currentBlock.move(-1, 0);
            }
        }
    }

    private boolean isGameOver() {
        if (tetrisGameGrid.isRowEmpty(0) == true) {
            return false;
        } else {
            return true;
        }
    }

    private void drawBlockIntoGrid() {
        BlockPosition currentBlockPosition = currentBlock.getPosition();

        for (BlockPosition p : currentBlock.shape()) {
            tetrisGameGrid.setCell(p.getRowIndex() + currentBlockPosition.getRowIndex(), p.getColIndex() + currentBlockPosition.getColIndex(), currentBlock.getId());
        }
    }

    private void deleteCurrentBlockFromGrid() {
        BlockPosition currentBlockPosition = currentBlock.getPosition();

        for (BlockPosition p : currentBlock.shape()) {
            tetrisGameGrid.setCell(p.getRowIndex() + currentBlockPosition.getRowIndex(), p.getColIndex() + currentBlockPosition.getColIndex(), 0);
        }
    }

    private void processUserInput(char keyCode) {
        if (keyCode == this.inputConfig.UP) {
            immediateBlockPlace();
        } else if (keyCode == this.inputConfig.DOWN) {
            moveBlockDown();
        } else if (keyCode == this.inputConfig.LEFT) {
            moveBlockLeft();
        } else if (keyCode == this.inputConfig.RIGHT) {
            moveBlockRight();
        } else if (keyCode == this.inputConfig.CW_SPIN) {
            rotateBlockCW();
        } else if (keyCode == this.inputConfig.CCW_SPIN) {
            rotateBlockCCW();
        } else if (keyCode == this.inputConfig.PAUSE) {

        } else if (keyCode == this.inputConfig.EXIT) {
            stopGame();
        }
    }

    private void drawNextBlock() {
        nextBlockGraphicsContext.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        BlockPosition[] nextBlockPositions = nextBlock.shape();

        int colIndex = 0;
        int rowIndex = 0;

        for (int i = 0; i < 4; i++) {
            colIndex = nextBlockPositions[i].getColIndex();
            rowIndex = nextBlockPositions[i].getRowIndex() + 1; // 더 잘보이게 하기 위해 행 인덱스에 1을 더해줌

            switch (nextBlock.getId()) {
                case 1: // I Block
                    nextBlockGraphicsContext.setFill(Block.IBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                case 2: // J Block
                    nextBlockGraphicsContext.setFill(Block.JBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                case 3: // L Block
                    nextBlockGraphicsContext.setFill(Block.LBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                case 4: // O Block
                    nextBlockGraphicsContext.setFill(Block.OBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                case 5: // S Block
                    nextBlockGraphicsContext.setFill(Block.SBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                case 6: // T Block
                    nextBlockGraphicsContext.setFill(Block.TBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                case 7:
                    nextBlockGraphicsContext.setFill(Block.ZBlock.blockColor.getBlockColor());
                    nextBlockGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
                    break;
                default:
                    nextBlockGraphicsContext.fillText(String.valueOf(' '), colIndex * TEXT_INTERVAL, rowIndex * TEXT_INTERVAL);
            }
        }
    }

    private void render() {
        gameGraphicsContext.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        for (int i = 0; i < ROW_SIZE; i++) {
            for (int j = 0; j < COL_SIZE; j++) {
                switch (tetrisGameGrid.getCell(i, j)) {
                    case 1: // I Block
                        gameGraphicsContext.setFill(Block.IBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    case 2: // J Block
                        gameGraphicsContext.setFill(Block.JBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    case 3: // L Block
                        gameGraphicsContext.setFill(Block.LBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    case 4: // O Block
                        gameGraphicsContext.setFill(Block.OBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    case 5: // S Block
                        gameGraphicsContext.setFill(Block.SBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    case 6: // T Block
                        gameGraphicsContext.setFill(Block.TBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    case 7:
                        gameGraphicsContext.setFill(Block.ZBlock.blockColor.getBlockColor());
                        gameGraphicsContext.fillText(String.valueOf(DEFAULT_BLOCK_TEXT), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                        break;
                    default:
                        gameGraphicsContext.fillText(String.valueOf(' '), j * TEXT_INTERVAL, i * TEXT_INTERVAL);
                }
            }
        }
    }

    private void update() {
        drawBlockIntoGrid();

        scoreLabel.setText(String.valueOf(score));

        if (isBlockPlaced == true) {
            int clearedRows = tetrisGameGrid.clearFullRows();

            if (clearedRows > 0) {
                score += 10 * clearedRows;
            }

            currentBlock = nextBlock;
            nextBlock = nextBlock();
            drawNextBlock();
            isBlockPlaced = false;

            if (isGameOver() == true) {
                gameStatus = GameStatus.GAMEOVER;
            }
        }
    }

    private void prepare() {
        gameGraphicsContext.setFill(new Color(0, 0, 0, 1.0));
        gameGraphicsContext.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void tick(long l) {
        blockMovingTimer.setCurrentTime(l);
        collideCheckingTimer.setCurrentTime(l);

        if ((float) blockMovingTimer.getElapsedTime() / 1000000000 > 1.0f) {
            moveBlockDown();
            blockMovingTimer.reset(l);
        }

        if (isBlockCollided == true) {
            if (collideCheckingTimer.isBlockPlaceTimeEnded() == true) {
                isBlockPlaced = true;
                isBlockCollided = false;
                collideCheckingTimer.reset(l);
            }

            if (collideCheckingTimer.isTimerStarted() == false) {
                collideCheckingTimer.setFirstBlockCollideTime(l);
            }
        } else {
            collideCheckingTimer.reset(l);
        }
    }

    private final int ROW_SIZE = 22;
    private final int COL_SIZE = 10;
    private final int TEXT_INTERVAL = 10;
    private final double CANVAS_WIDTH;
    private final double CANVAS_HEIGHT;
    private final char DEFAULT_BLOCK_TEXT = 'O';
    private AnimationTimer animationTimer;
    private ConfigRepositoryImpl configRepository;
    private InputManager inputManager;
    private InputConfig inputConfig;
    private TetrisGrid tetrisGameGrid;
    private final BlockQueue blockQueue;
    private CurrentBlock currentBlock;
    private CurrentBlock nextBlock;
    private final GraphicsContext gameGraphicsContext;
    private final GraphicsContext nextBlockGraphicsContext;
    private GameStatus gameStatus;
    private Label scoreLabel;
    private Timer blockMovingTimer;
    private BlockCollideTimer collideCheckingTimer;
    private int score;
    private boolean isGameStarted;
    private boolean isBlockPlaced;
    private boolean isBlockCollided;
}
