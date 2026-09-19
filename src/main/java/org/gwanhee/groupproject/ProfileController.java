package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {
    @FXML
    private Button addPayment;

    @FXML
    private Label emailLabel;

    @FXML
    private Label customerIdLabel;

    @FXML
    private Label paymentLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private void initialize() {
        loadProfileData();
    }



    private void loadProfileData() {
        String loginId = Session.getCurrentLoginId();

        if (loginId == null || loginId.isEmpty()) {
            AlertUtil.errorAlert("Session Error", "No logged-in customer found.");
            return;
        }

        Customer customer = CustomerDAO.findByLoginId(loginId);

        if (customer == null) {
            AlertUtil.errorAlert("Profile Error", "Could not load customer information.");
            return;
        }

        emailLabel.setText(customer.getEmail());
        customerIdLabel.setText(customer.getLogInID());

        if (customer.getAddress() == null || customer.getAddress().isEmpty()) {
            addressLabel.setText("No delivery address");
        } else {
            addressLabel.setText(customer.getAddress());
        }

        String paymentText = PaymentMethodDAO.getPaymentSummary(loginId);
        paymentLabel.setText(paymentText);
    }

    @FXML
    public void addPaymentMethod() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Payment.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 300, 400);

        Stage popupStage = new Stage();
        popupStage.setTitle("Add Payment");
        popupStage.setScene(scene);
        popupStage.setResizable(true);
        popupStage.show();
    }

    @FXML
    private void changeAddress() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Address.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 300, 400);

        NewAddressController controller = fxmlLoader.getController();

        Stage popupStage = new Stage();
        popupStage.setTitle("Change Delivery Address");
        popupStage.setScene(scene);
        popupStage.setResizable(false);

        popupStage.initOwner(addPayment.getScene().getWindow());
        popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

        popupStage.showAndWait();

        if (controller.isAddressUpdated()) {
            loadProfileData();
        }
    }

    @FXML
    private void delete_deactivate() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Delete.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) addPayment.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


    @FXML
    private void backtomain() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Main.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) addPayment.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}

