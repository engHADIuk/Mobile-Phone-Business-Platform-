package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ActiveController {
    @FXML
    private PasswordField passwordfield;

    @FXML
    private TextField idfield;

    @FXML
    private Label text;

    @FXML
    private void activeaccount() throws IOException {
        String id = idfield.getText().trim();
        String password = passwordfield.getText();

        if (id.isEmpty() || password.isEmpty()) {
            AlertUtil.warningAlert(
                    "Missing Information",
                    "Please enter ID and password"
            );
            return;
        }

        boolean success = CustomerDAO.activate(id, password);

        if (success) {
            CustomerDAO.activateCustomer(id);
            AlertUtil.infoAlert("Customer Activated", "Successfully Activated");

            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/gwanhee/groupproject/LogIn.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), 350, 600);

            Stage stage = (Stage) text.getScene().getWindow();
            stage.setTitle("Main");
            stage.setScene(scene);
            stage.show();
        } else {
            AlertUtil.errorAlert("Activation Failed", "Invalid ID or Password");
        }
    }

    @FXML
    private void back(MouseEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Help.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
