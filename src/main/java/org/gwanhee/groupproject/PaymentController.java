package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class PaymentController {
    @FXML
    private Button returnhome;

    @FXML
    private Button deleteCardButton;

    @FXML
    private Label currentPaymentLabel;

    @FXML
    private void initialize() {
        String loginId = Session.getCurrentLoginId();

        if (loginId == null || loginId.isEmpty()) {
            currentPaymentLabel.setText("No logged-in user");
            return;
        }

        currentPaymentLabel.setText(PaymentMethodDAO.getPaymentSummary(loginId));
    }

    @FXML
    public void addPayment() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/NewCard.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 400, 300);

        Stage popupStage = new Stage();
        popupStage.setTitle("Add Payment Method");
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.show();

        popupStage.initOwner(returnhome.getScene().getWindow());
        popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        popupStage.showAndWait();

        String loginId = Session.getCurrentLoginId();
        currentPaymentLabel.setText(
                PaymentMethodDAO.getPaymentSummary(loginId)
        );
    }

    @FXML
    public void deletePayment() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/DeleteCard.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 430, 360);

        DeleteCardController controller = fxmlLoader.getController();
        controller.setCustomerId(Session.getCurrentLoginId());

        Stage popupStage = new Stage();
        popupStage.setTitle("Delete Payment Card");
        popupStage.setScene(scene);
        popupStage.setResizable(false);

        popupStage.initOwner(returnhome.getScene().getWindow());
        popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

        popupStage.showAndWait();

        String loginId = Session.getCurrentLoginId();
        currentPaymentLabel.setText(
                PaymentMethodDAO.getPaymentSummary(loginId)
        );
    }

    @FXML
    public void returnbutton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Profile.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) returnhome.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
