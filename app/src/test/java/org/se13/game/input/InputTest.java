package org.se13.game.input;

import javafx.scene.Group;
import javafx.scene.Scene;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.se13.game.action.TetrisAction;

import static org.junit.jupiter.api.Assertions.*;

public class InputTest {
    @Test
    @DisplayName("InputManager 클래스 동작 테스트")
    void inputTest() {
        InputManager inputManager = new InputManager();
        inputManager.add(TetrisAction.CONNECT);
        assertTrue(inputManager.peekInput());
        assertEquals(inputManager.getInput(), TetrisAction.CONNECT);

        inputManager.reset();

        assertFalse(inputManager.peekInput());
        assertNull(inputManager.getInput());
    }
}
