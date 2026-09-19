package uk.ac.brunel.delivery.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

public final class LaunchpadController {
    @FXML private Button launchButton;

    @FXML
    private void initialize() {
        Theme.stylePrimary(launchButton);
    }

    @FXML
    private void openDeliveryLogistics() {
        SceneManager.getInstance().showLogin();
    }
}
