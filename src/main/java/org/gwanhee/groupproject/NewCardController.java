package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewCardController {

    @FXML
    private TextField cardHolderField;

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField expiryField;

    @FXML
    private PasswordField cvcField;

    @FXML
    private void saveCard() {
        String cardHolder = cardHolderField.getText().trim();
        String cardNumber = cardNumberField.getText().trim().replace(" ", "");
        String expiry = expiryField.getText().trim();
        String cvc = cvcField.getText().trim();

        if (cardHolder.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() || cvc.isEmpty()) {
            AlertUtil.warningAlert("Missing Information", "Please fill in all card fields.");
            return;
        }

        if (!cardNumber.matches("\\d+")) {
            AlertUtil.warningAlert("Invalid Card", "Card number must contain only digits.");
            return;
        }

        if (!cardNumber.matches("\\d{12,19}")) {
            AlertUtil.warningAlert(
                    "Invalid Card",
                    "Card number must be 12 to 19 digits."
            );
            return;
        }

        if (!cvc.matches("\\d+")) {
            AlertUtil.warningAlert("Invalid CVC", "CVC must contain only digits.");
            return;
        }

        if (!cvc.matches("\\d{3,4}")) {
            AlertUtil.warningAlert(
                    "Invalid CVC",
                    "CVC must be 3 or 4 digits."
            );
            return;
        }

        String customerId = Session.getCurrentLoginId();

        if (customerId == null || customerId.isEmpty()) {
            AlertUtil.errorAlert("Session Error", "No logged-in customer found.");
            return;
        }

        boolean success = PaymentMethodDAO.saveCard(
                customerId,
                cardHolder,
                cardNumber,
                expiry,
                cvc
        );

        if (success) {
            AlertUtil.infoAlert("Card Saved", "Payment card has been saved.");
            closeWindow();
        } else {
            AlertUtil.errorAlert("Save Failed", "Could not save payment card.");
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cardHolderField.getScene().getWindow();
        stage.close();
    }
}