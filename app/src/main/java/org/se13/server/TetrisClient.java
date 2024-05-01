package org.se13.server;

import org.se13.view.tetris.TetrisGameEndData;
import org.se13.view.tetris.TetrisState;
import org.se13.view.tetris.TetrisStateRepository;

public class TetrisClient {

    private int userId;
    private TetrisStateRepository repository;

    public TetrisClient(int userId, TetrisStateRepository repository) {
        this.userId = userId;
        this.repository = repository;
    }

    public void response(TetrisState state) {
        repository.response(state);
    }

    public void gameOver(int score, boolean isItemMode, String difficulty) {
        repository.gameOver(new TetrisGameEndData(score, isItemMode, difficulty));
    }

    public int getUserId() {
        return userId;
    }
}