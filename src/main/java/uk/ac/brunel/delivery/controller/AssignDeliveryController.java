package uk.ac.brunel.delivery.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.Driver;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.service.ValidationException;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AssignDeliveryController {
    @FXML private Label deliveryIdLabel;
    @FXML private Label orderReferenceLabel;
    @FXML private Label addressLabel;
    @FXML private Label warehouseLabel;
    @FXML private Label currentDriverLabel;
    @FXML private Label errorLabel;
    @FXML private ComboBox<Driver> driverCombo;
    @FXML private DatePicker scheduleDate;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Spinner<Integer> etaSpinner;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Delivery delivery;

    @FXML
    private void initialize() {
        Theme.styleCombo(driverCombo);
        scheduleDate.setStyle("-fx-background-color: #181919; -fx-border-color: #2B2D2D; -fx-border-radius: 8;");
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalDateTime.now().getHour()));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        etaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 480, 45, 5));
        String spinnerStyle = "-fx-background-color: #181919; -fx-border-color: #2B2D2D; -fx-border-radius: 8;";
        hourSpinner.setStyle(spinnerStyle);
        minuteSpinner.setStyle(spinnerStyle);
        etaSpinner.setStyle(spinnerStyle);
        Theme.stylePrimary(saveButton);
        Theme.styleSecondary(cancelButton);
        driverCombo.setItems(FXCollections.observableArrayList(
                AppServices.deliveries().findDrivers().stream().filter(Driver::available).toList()
        ));
        scheduleDate.setValue(LocalDate.now());
        errorLabel.setText("");
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        deliveryIdLabel.setText(delivery.deliveryId());
        orderReferenceLabel.setText(delivery.orderReference());
        addressLabel.setText(delivery.deliveryAddress());
        warehouseLabel.setText(delivery.warehouseLocation());
        currentDriverLabel.setText(delivery.driverDisplayName());
        etaSpinner.getValueFactory().setValue(delivery.estimatedMinutes());

        if (delivery.scheduledStart() != null) {
            scheduleDate.setValue(delivery.scheduledStart().toLocalDate());
            hourSpinner.getValueFactory().setValue(delivery.scheduledStart().getHour());
            minuteSpinner.getValueFactory().setValue(delivery.scheduledStart().getMinute());
        }
        if (delivery.assignedDriverId() != null) {
            driverCombo.getItems().stream()
                    .filter(driver -> driver.driverId().equals(delivery.assignedDriverId()))
                    .findFirst()
                    .ifPresent(driverCombo.getSelectionModel()::select);
        }
    }

    @FXML
    private void saveAssignment() {
        try {
            LocalDate date = scheduleDate.getValue();
            LocalDateTime start = date == null ? null
                    : date.atTime(hourSpinner.getValue(), minuteSpinner.getValue());
            AppServices.deliveries().assignDriver(
                    delivery,
                    driverCombo.getValue(),
                    start,
                    etaSpinner.getValue(),
                    SessionContext.actorName()
            );
            AppServices.events().publish();
            SceneManager.getInstance().showDeliveries();
        } catch (ValidationException exception) {
            errorLabel.setText(exception.getMessage());
            errorLabel.setStyle("-fx-text-fill: " + Theme.RED + "; -fx-font-weight: bold;");
        } catch (RuntimeException exception) {
            errorLabel.setText("Assignment failed: " + exception.getMessage());
            errorLabel.setStyle("-fx-text-fill: " + Theme.RED + "; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void cancel() {
        SceneManager.getInstance().showDeliveries();
    }
}
