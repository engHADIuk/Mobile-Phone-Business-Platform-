package com.ecommerce.finance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginController {

    @FXML private TextField usernameField; // will accept email
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button eyeToggleBtn;
    @FXML private HBox emailWrap;
    @FXML private HBox passwordWrap;
    @FXML private Label messageLabel;
    @FXML private ToggleButton adminToggleBtn;
    @FXML private ToggleButton employeeToggleBtn;
    @FXML private VBox glassCard;
    @FXML private HBox statCard1;
    @FXML private HBox statCard2;
    @FXML private HBox statCard3;
    @FXML private VBox statCard4;

    // Hardcoded users – only for demo; replace with hashed password in production
    private static final Map<String, String> DUMMY_USERS = new HashMap<>();
    static {
        DUMMY_USERS.put("hadi7@email.com", "hadi7"); // admin
        DUMMY_USERS.put("leo@email.com", "leo10"); // employee
    }

    @FXML
    public void initialize() {
        // Set focus on username field and allow Enter key for login
        passwordField.setOnAction(e -> handleLogin());
        passwordVisibleField.setOnAction(e -> handleLogin());
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        usernameField.focusedProperty().addListener((obs, was, isNow) -> toggleFocusStyle(emailWrap, isNow));
        passwordField.focusedProperty().addListener((obs, was, isNow) -> toggleFocusStyle(passwordWrap, isNow));
        passwordVisibleField.focusedProperty().addListener((obs, was, isNow) -> toggleFocusStyle(passwordWrap, isNow));

        // Don't allow the role toggle to be fully deselected
        adminToggleBtn.selectedProperty().addListener((obs, was, isNow) -> {
            if (!isNow && !employeeToggleBtn.isSelected()) adminToggleBtn.setSelected(true);
        });
        employeeToggleBtn.selectedProperty().addListener((obs, was, isNow) -> {
            if (!isNow && !adminToggleBtn.isSelected()) employeeToggleBtn.setSelected(true);
        });

        // Entrance animations (purely visual)
        Node[] statCards = {statCard1, statCard2, statCard3, statCard4};
        AnimationHelper.staggerIn(statCards);
        AnimationHelper.fadeSlideIn(glassCard, 150);
    }

    private void toggleFocusStyle(HBox wrap, boolean focused) {
        if (focused) {
            if (!wrap.getStyleClass().contains("input-focused")) wrap.getStyleClass().add("input-focused");
        } else {
            wrap.getStyleClass().remove("input-focused");
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean revealing = !passwordVisibleField.isVisible();
        passwordVisibleField.setVisible(revealing);
        passwordVisibleField.setManaged(revealing);
        passwordField.setVisible(!revealing);
        passwordField.setManaged(!revealing);
        eyeToggleBtn.setText(revealing ? "Hide" : "Show");
    }

    @FXML
    private void forgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Reset");
        alert.setHeaderText(null);
        alert.setContentText("Please contact your system administrator to reset your password.");
        DialogHelper.styleAlert(alert);
        alert.showAndWait();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter your email and password.");
            return;
        }

        // Authenticate
        if (!DUMMY_USERS.containsKey(username) || !DUMMY_USERS.get(username).equals(password)) {
            messageLabel.setText("Invalid credentials. Please try again.");
            return;
        }

        // Determine role based on username
        String role = username.equals("hadi7@email.com") ? "admin" : "employee";

        // Make sure the account role matches the selected login tab
        boolean wantsAdmin = adminToggleBtn.isSelected();
        if (wantsAdmin && !role.equals("admin")) {
            messageLabel.setText("This account is an Employee account. Switch to Employee Login.");
            return;
        }
        if (!wantsAdmin && !role.equals("employee")) {
            messageLabel.setText("This account is an Admin account. Switch to Admin Login.");
            return;
        }

        // Set session (store just the display name, not the full email, for a cleaner greeting)
        String displayName = username.contains("@") ? username.substring(0, username.indexOf('@')) : username;
        SessionManager.getInstance().setSessionToken(UUID.randomUUID().toString());
        SessionManager.getInstance().setCurrentUser(displayName);
        SessionManager.getInstance().setRole(role);

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            if (role.equals("admin")) {
                Scene dashScene = DashboardController.createDashboardScene(stage);
                stage.setScene(dashScene);
                stage.setTitle("Admin Dashboard");
            } else {
                Scene empScene = EmployeeDashboardController.createEmployeeDashboardScene(stage);
                stage.setScene(empScene);
                stage.setTitle("Employee Dashboard");
            }
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading dashboard.");
        }
    }
}