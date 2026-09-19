package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewAddressController {
    @FXML
    private TextField addressLineField;

    @FXML
    private Label currentAddressLabel;

    private boolean addressUpdated = false;

    public boolean isAddressUpdated() {
        return addressUpdated;
    }

    @FXML
    private void initialize() {
        String loginId = Session.getCurrentLoginId();

        if (loginId == null || loginId.isEmpty()) {
            currentAddressLabel.setText("No logged-in customer");
            return;
        }

        Customer customer =
                CustomerDAO.findByLoginId(loginId);

        if (customer == null
                || customer.getAddress() == null
                || customer.getAddress().isEmpty()) {
            currentAddressLabel.setText(
                    "No delivery address"
            );
        } else {
            currentAddressLabel.setText(
                    customer.getAddress()
            );
        }
    }

    @FXML
    private void saveAddress() {
        String addressLine = addressLineField.getText().trim();

        if (addressLine.isEmpty()) {
            AlertUtil.warningAlert("Missing Information", "Please fill in all address fields.");
            return;
        }

        String loginId = Session.getCurrentLoginId();

        if (loginId == null || loginId.isEmpty()) {
            AlertUtil.errorAlert("Session Error", "No logged-in customer found.");
            return;
        }

        boolean success = CustomerDAO.updateAddress(loginId, addressLine);

        if (success) {
            addressUpdated = true;
            AlertUtil.infoAlert("Address Saved", "Delivery address has been saved.");
            closeWindow();
        } else {
            AlertUtil.errorAlert("Save Failed", "Could not save delivery address.");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) addressLineField.getScene().getWindow();
        stage.close();
    }
}