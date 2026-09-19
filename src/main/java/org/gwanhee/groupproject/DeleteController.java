package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.awt.*;
import java.io.IOException;

public class DeleteController {
    @FXML
    private Button deleteButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void deletebutton() throws IOException {
        String id = Session.getCurrentLoginId();
        String password = passwordField.getText();

        boolean success = CustomerDAO.login(id, password);

        if (success) {
            CustomerDAO.deleteCustomer(id);
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/gwanhee/groupproject/LogI  n.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), 350, 600);

            Stage stage = (Stage) deleteButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } else {
            AlertUtil.errorAlert("Invalid Password", "Invalid Password");
        }
    }

    @FXML
    private void deactivatebutton() throws IOException {
        String id = Session.getCurrentLoginId();
        String password = passwordField.getText();

        boolean success = CustomerDAO.login(id, password);

        if (success) {
            CustomerDAO.deactivateCustomer(id);
            AlertUtil.infoAlert("Customer Deactivated", "Customer Deactivated");
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/gwanhee/groupproject/Login.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), 350, 600);

            Stage stage = (Stage) deleteButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } else {
            AlertUtil.errorAlert("Invalid Password", "Invalid Password");
        }
    }

    public void returnbutton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Profile.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}

