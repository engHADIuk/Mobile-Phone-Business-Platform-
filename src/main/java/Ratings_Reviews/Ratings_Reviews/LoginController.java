package Ratings_Reviews.Ratings_Reviews;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void login() {
        try {
            User user = App.database().authenticate(emailField.getText(), passwordField.getText());
            Session.login(user);
            if ("ADMIN".equals(user.getRole())) App.showAdminDashboard();
            else App.showCustomerDashboard();
        } catch (ValidationException exception) {
            messageLabel.setText(exception.getMessage());
        } catch (IOException | IllegalStateException exception) {
            messageLabel.setText("Unable to open the next screen: " + exception.getMessage());
        }
    }

    @FXML
    private void useCustomerDemo() {
        emailField.setText("customer@example.com");
        passwordField.setText("customer123");
        messageLabel.setText("Demo customer loaded. This account has verified purchases.");
    }

    @FXML
    private void useAdminDemo() {
        emailField.setText("admin@valmora.com");
        passwordField.setText("admin123");
        messageLabel.setText("Demo administrator loaded.");
    }
}
