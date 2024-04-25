package org.se13.view.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.se13.NavGraph;
import org.se13.SE13Application;
import org.se13.view.base.BaseController;
import org.se13.view.setting.SettingScreenController;

public class ControllerTest {
    public static NavGraph navController;

    @Test
    @DisplayName("BaseController test")
    void testBaseController() {
        BaseController baseController = new BaseController();
        baseController.onCreate();
        baseController.onStart();
    }

    @Test
    @DisplayName("SettingScreenController test")
    void testSettingScreenController() {
        new Thread(() -> {
            SE13Application.isTestMode = true;
            SE13Application.main(new String[0]);
        }).start();

        SettingScreenController settingScreenController = new SettingScreenController();
        settingScreenController.isTestMode = true;

        settingScreenController.initialize();

    }
}
