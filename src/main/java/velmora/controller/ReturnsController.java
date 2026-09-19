package velmora.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import velmora.SceneManager;
import velmora.db.OrderItemDao;
import velmora.db.ProductDao;
import velmora.db.ReturnDao;
import velmora.db.ReturnedItemDao;
import velmora.db.UserDao;
import velmora.model.Order;
import velmora.model.OrderHistory;
import velmora.model.Session;

public class ReturnsController implements Initializable {

    private static final int RETURN_WINDOW_DAYS = 30;

    @FXML private ListView<Order> deliveredOrdersListView;
    @FXML private Label placeholderLabel;
    @FXML private Label detailId, detailSummary, detailDate, eligibilityLabel, returnedNotice, returnError;
    @FXML private VBox itemsCheckBox;
    @FXML private TextArea reasonField;
    @FXML private Label confirmSectionLabel;
    @FXML private TextField confirmEmailField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button startReturnButton;

    private final ReturnDao returnDao = new ReturnDao();
    private final ReturnedItemDao returnedItemDao = new ReturnedItemDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final ProductDao productDao = new ProductDao();
    private final UserDao userDao = new UserDao();

    private FilteredList<Order> deliveredOrders;
    private Order selected;
    private List<OrderItemDao.Row> selectedOrderLines;
    private final Map<OrderItemDao.Row, CheckBox> lineCheckBoxes = new LinkedHashMap<>();
    private boolean awaitingConfirm = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deliveredOrders = new FilteredList<>(OrderHistory.getInstance().getOrders(),
                o -> o.getStatus() == Order.Status.DELIVERED);
        deliveredOrdersListView.setItems(deliveredOrders);
        deliveredOrdersListView.setCellFactory(list -> buildRow());

        deliveredOrdersListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldOrder, newOrder) -> showOrder(newOrder));

        Timeline refresher = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            deliveredOrders.setPredicate(deliveredOrders.getPredicate());
            deliveredOrdersListView.refresh();
        }));
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();
    }

    private ListCell<Order> buildRow() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setGraphic(null);
                    return;
                }
                Label idLabel = new Label(order.getId());
                idLabel.getStyleClass().add("dash-card-title");

                Label summaryLabel = new Label(order.getSummary());
                summaryLabel.getStyleClass().add("dash-card-desc");
                summaryLabel.setWrapText(true);

                Label dateLabel = new Label(order.getFormattedDate());
                dateLabel.getStyleClass().add("dash-card-desc");

                VBox box = new VBox(4, idLabel, summaryLabel, dateLabel);
                HBox row = new HBox(box);
                row.setPadding(new Insets(10, 4, 10, 4));
                setGraphic(row);
            }
        };
    }

    private boolean isEligible(Order order) {
        long daysSinceDelivery = ChronoUnit.DAYS.between(order.getDate(), java.time.LocalDate.now());
        return daysSinceDelivery <= RETURN_WINDOW_DAYS;
    }

    private void showOrder(Order order) {
        selected = order;
        awaitingConfirm = false;
        boolean hasSelection = order != null;

        placeholderLabel.setVisible(!hasSelection);
        placeholderLabel.setManaged(!hasSelection);
        returnedNotice.setVisible(false);
        returnedNotice.setManaged(false);
        hideError();
        reasonField.clear();
        hideConfirmFields();
        itemsCheckBox.getChildren().clear();
        lineCheckBoxes.clear();

        for (Label label : new Label[]{detailId, detailSummary, detailDate, eligibilityLabel}) {
            label.setVisible(hasSelection);
            label.setManaged(hasSelection);
        }
        reasonField.setVisible(hasSelection);
        reasonField.setManaged(hasSelection);
        startReturnButton.setText("Start Return");

        if (!hasSelection) {
            startReturnButton.setVisible(false);
            startReturnButton.setManaged(false);
            return;
        }

        detailId.setText(order.getId());
        detailSummary.setText(order.getSummary());
        detailDate.setText("Delivered " + order.getFormattedDate());

        boolean eligible = isEligible(order);
        eligibilityLabel.getStyleClass().removeAll("status-delivered", "status-cancelled");
        if (eligible) {
            eligibilityLabel.setText("Eligible for return");
            eligibilityLabel.getStyleClass().add("status-delivered");
        } else {
            eligibilityLabel.setText("Return window has closed");
            eligibilityLabel.getStyleClass().add("status-cancelled");
        }

        selectedOrderLines = orderItemDao.findByOrderId(order.getId());
        for (OrderItemDao.Row line : selectedOrderLines) {
            CheckBox box = new CheckBox(String.format("%s — %s, %s  x%d",
                    line.productName, line.color, line.storage, line.qty));
            lineCheckBoxes.put(line, box);
            itemsCheckBox.getChildren().add(box);
        }

        startReturnButton.setVisible(eligible);
        startReturnButton.setManaged(eligible);
    }

    private void hideConfirmFields() {
        confirmEmailField.clear();
        confirmPasswordField.clear();
        confirmSectionLabel.setVisible(false);
        confirmSectionLabel.setManaged(false);
        confirmEmailField.setVisible(false);
        confirmEmailField.setManaged(false);
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
    }

    private void showError(String message) {
        returnError.setText(message);
        returnError.setVisible(true);
        returnError.setManaged(true);
    }

    private void hideError() {
        returnError.setVisible(false);
        returnError.setManaged(false);
    }

    @FXML
    private void startReturn() {
        if (selected == null) return;

        List<OrderItemDao.Row> chosen = new ArrayList<>();
        for (Map.Entry<OrderItemDao.Row, CheckBox> entry : lineCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) chosen.add(entry.getKey());
        }

        if (chosen.isEmpty()) {
            showError("Select at least one item to return.");
            return;
        }
        String reason = reasonField.getText() == null ? "" : reasonField.getText().trim();
        if (reason.isEmpty()) {
            showError("Please give a reason for the return.");
            return;
        }
        hideError();

        if (!awaitingConfirm) {
            awaitingConfirm = true;
            confirmSectionLabel.setVisible(true);
            confirmSectionLabel.setManaged(true);
            confirmEmailField.setVisible(true);
            confirmEmailField.setManaged(true);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            startReturnButton.setText("Confirm Return");
            return;
        }

        String email = confirmEmailField.getText() == null ? "" : confirmEmailField.getText().trim();
        String password = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Enter your email and password to confirm the return.");
            return;
        }
        if (!email.equalsIgnoreCase(Session.getEmail()) || !userDao.checkCredentials(email, password)) {
            showError("That email and password don't match your logged-in account.");
            return;
        }

        int returnId = returnDao.insert(selected.getId(), reason);

        for (OrderItemDao.Row line : chosen) {
            returnedItemDao.insert(returnId, new ReturnedItemDao.Row(line.productName, line.color, line.storage, line.qty));
            productDao.adjustStock(line.storage, line.qty);
        }

        if (chosen.size() == selectedOrderLines.size()) {
            OrderHistory.getInstance().updateStatus(selected, Order.Status.RETURNED);
            deliveredOrders.setPredicate(deliveredOrders.getPredicate());
        }

        startReturnButton.setVisible(false);
        startReturnButton.setManaged(false);
        reasonField.setVisible(false);
        reasonField.setManaged(false);
        hideConfirmFields();
        itemsCheckBox.getChildren().clear();
        returnedNotice.setVisible(true);
        returnedNotice.setManaged(true);
    }

    @FXML
    private void goBack() {
        SceneManager.show("dashboard");
    }
}
