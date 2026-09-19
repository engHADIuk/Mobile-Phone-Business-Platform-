package velmora.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import velmora.SceneManager;
import velmora.db.UserDao;
import velmora.model.Session;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginError;

    private final UserDao userDao = new UserDao();

    @FXML
    private void login() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        if (!userDao.checkCredentials(email, password)) {
            showError("Incorrect email or password.");
            return;
        }

        String name = userDao.findName(email);
        Session.login(name, email);
        SceneManager.show("dashboard");
    }

    private void showError(String message) {
        loginError.setText(message);
        loginError.setVisible(true);
        loginError.setManaged(true);
    }
}
