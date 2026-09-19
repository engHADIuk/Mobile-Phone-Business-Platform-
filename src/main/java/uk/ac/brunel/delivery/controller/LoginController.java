package uk.ac.brunel.delivery.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import uk.ac.brunel.delivery.model.SessionUser;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import java.util.Optional;

public final class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button signInButton;
    @FXML private Button launchpadButton;

    @FXML
    private void initialize() {
        Theme.styleInput(usernameField);
        Theme.styleInput(passwordField);
        Theme.stylePrimary(signInButton);
        errorLabel.setText("");

        // Only shown when this component was opened from the Velmora launchpad;
        // running Delivery & Logistics on its own leaves the control hidden.
        boolean hosted = SceneManager.getInstance().canReturnToLaunchpad();
        launchpadButton.setVisible(hosted);
        launchpadButton.setManaged(hosted);
        if (hosted) {
            Theme.styleSecondary(launchpadButton);
        }
    }

    @FXML
    private void backToLaunchpad() {
        SceneManager.getInstance().returnToLaunchpad();
    }

    @FXML
    private void signIn() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isBlank() || password == null || password.isBlank()) {
            showError("Enter both your username and password.");
            return;
        }

        try {
            Optional<SessionUser> user = AppServices.auth().authenticate(username, password);
            if (user.isEmpty()) {
                showError("Incorrect username or password, or the account is inactive.");
                passwordField.clear();
                return;
            }
            SessionContext.login(user.get());
            if (user.get().isDriver()) {
                SceneManager.getInstance().showDriverDashboard();
            } else {
                SceneManager.getInstance().showAdminDashboard();
            }
        } catch (RuntimeException exception) {
            showError("Login could not be completed: " + exception.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: " + Theme.RED + "; -fx-font-size: 12;");
    }
}
