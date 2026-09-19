package org.gwanhee.groupproject;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.ArrayList;

public class DeleteCardController {

    @FXML
    private ListView<PaymentCard> cardListView;

    private String customerId;

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        loadCards();
    }

    private void loadCards() {
        ArrayList<PaymentCard> cards = PaymentMethodDAO.getCardsByCustomerId(customerId);

        cardListView.setItems(FXCollections.observableArrayList(cards));

        if (cards.isEmpty()) {
            AlertUtil.infoAlert("No Cards", "There are no registered cards.");
        }
    }

    @FXML
    private void deleteSelectedCard() {
        PaymentCard selectedCard = cardListView.getSelectionModel().getSelectedItem();

        if (selectedCard == null) {
            AlertUtil.warningAlert("No Selection", "Please select a card to delete.");
            return;
        }

        boolean success = PaymentMethodDAO.deleteCardById(selectedCard.getPaymentMethodId());

        if (success) {
            AlertUtil.infoAlert("Card Deleted", "The selected card has been deleted.");
            loadCards();
        } else {
            AlertUtil.errorAlert("Delete Failed", "Could not delete the selected card.");
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cardListView.getScene().getWindow();
        stage.close();
    }
}