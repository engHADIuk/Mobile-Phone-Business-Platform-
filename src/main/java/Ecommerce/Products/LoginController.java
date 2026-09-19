package Ecommerce.Products;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private Label portalLabel;
    @FXML private Label restrictedBadge;
    @FXML private Label domainHelper;
    @FXML private Label errorLabel;
    @FXML private Label demoFillLabel;
    @FXML private Label switchPortalLink;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private boolean adminPortal = false; // false = Customer, true = Admin

    @FXML
    private void handleLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String email = emailField.getText();
        String password = passwordField.getText();

        String error = Controller.checkLogin(email, password, adminPortal);

        if (error != null) {
            errorLabel.setText(error);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        try {
            if (adminPortal) {
                App.setRoot("admin", 1100, 700);
            } else {
                App.setRoot("customer", 1100, 700);
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Could not load the next screen: " + e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    @FXML
    private void handleAutoFill() {
        if (adminPortal) {
            emailField.setText("admin@velmora.com");
            passwordField.setText("admin123");
        } else {
            emailField.setText("customer@example.com");
            passwordField.setText("customer123");
        }
    }

    @FXML
    private void handleSwitchPortal() {
        adminPortal = !adminPortal;
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        emailField.clear();
        passwordField.clear();

        if (adminPortal) {
            portalLabel.setText("Admin Portal");
            restrictedBadge.setVisible(true);
            restrictedBadge.setManaged(true);
            domainHelper.setVisible(true);
            domainHelper.setManaged(true);
            demoFillLabel.setText("Click to auto-fill: admin@velmora.com");
            switchPortalLink.setText("← Customer Login");
        } else {
            portalLabel.setText("Customer Portal");
            restrictedBadge.setVisible(false);
            restrictedBadge.setManaged(false);
            domainHelper.setVisible(false);
            domainHelper.setManaged(false);
            demoFillLabel.setText("Click to auto-fill: customer@example.com");
            switchPortalLink.setText("Admin Login →");
        }
    }
}