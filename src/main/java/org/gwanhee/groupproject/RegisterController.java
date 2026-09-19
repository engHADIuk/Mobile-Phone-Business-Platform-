package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField registName;

    @FXML
    private TextField registEmail;

    @FXML
    private TextField registAddress;

    @FXML
    private TextField registID;

    @FXML
    private PasswordField registPassword;

    @FXML
    private Label welcomeText;

    @FXML
    private void registrationDone() throws IOException {
        String name = registName.getText().trim();
        String address = registAddress.getText().trim();
        String id = registID.getText().trim();
        String password = registPassword.getText();
        String email = registEmail.getText().trim();

        if (name.isEmpty() || id.isEmpty() || password.isEmpty()) {
            AlertUtil.warningAlert("Missing information", "There is missing information");
            return;
        }

        if(!email.contains("@") || email.length() < 3 || !email.endsWith(".com")) {
            AlertUtil.warningAlert("Invalid Email", "Please enter valid email");
            return;
        }

        if (CustomerDAO.findByLoginId(id) != null) {
            AlertUtil.warningAlert("Register failed, ID, or Password is incorrect", "Register failed");
            return;
        }

        if (CustomerDAO.emailExists(email)) {
            AlertUtil.warningAlert("Register Failed", "This email is already used.");
            return;
        }

        Customer customer = new Customer(name, email, id, password, address);

        boolean success = CustomerDAO.addCustomer(customer);

        if (!success) {
            AlertUtil.errorAlert(
                    "Register Failed",
                    "Could not create account."
            );
        } else {
            welcomeText.setText("Registered successfully: " + name);

            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource(
                            "/org/gwanhee/groupproject/LogIn.fxml"
                    )
            );

            Scene scene = new Scene(fxmlLoader.load(), 350, 600);

            Stage stage =
                    (Stage) registName.getScene().getWindow();

            stage.setScene(scene);
            stage.show();
        }
    }
}
