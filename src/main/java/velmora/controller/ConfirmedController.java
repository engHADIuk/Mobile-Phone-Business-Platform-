package velmora.controller;

import javafx.fxml.FXML;
import velmora.SceneManager;

public class ConfirmedController {

    @FXML
    private void goBack() {
        SceneManager.show("dashboard");
    }
}
