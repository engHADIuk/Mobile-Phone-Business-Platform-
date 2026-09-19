package velmora.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import velmora.SceneManager;
import velmora.db.UserDao;
import velmora.model.Cart;
import velmora.model.CartItem;
import velmora.model.OrderHistory;
import velmora.model.OrderResult;
import velmora.model.Session;

public class CartController implements Initializable {

    private static final Pattern CARD_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");

    @FXML private ListView<CartItem> cartListView;
    @FXML private Label subtotalLabel, shippingLabel, totalLabel;
    @FXML private VBox checkoutForm;
    @FXML private TextField shippingAddressField;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardExpiryField;
    @FXML private PasswordField cardCvvField;
    @FXML private TextField confirmEmailField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label orderError;
    @FXML private Button confirmButton;

    private final Cart cart = Cart.getInstance();
    private final UserDao userDao = new UserDao();
    private boolean formRevealed = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cartListView.setItems(cart.getItems());
        cartListView.setCellFactory(list -> buildRow());
        cart.getItems().addListener((javafx.collections.ListChangeListener<CartItem>) c -> refreshTotals());
        refreshTotals();
    }

    private ListCell<CartItem> buildRow() {
        return new ListCell<>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label name = new Label(item.getProduct().toString());
                name.getStyleClass().add("cart-item-name");

                Label qty = new Label("x" + item.getQty());
                qty.getStyleClass().add("cart-item-meta");

                Label price = new Label(String.format("$%.0f", item.getLineTotal()));
                price.getStyleClass().add("cart-item-price");

                Button minus = new Button("−");
                minus.getStyleClass().add("qty-mini-btn");
                minus.setOnAction(e -> {
                    cart.updateQty(item, Math.max(1, item.getQty() - 1));
                    refreshTotals();
                    getListView().refresh();
                });

                Button plus = new Button("+");
                plus.getStyleClass().add("qty-mini-btn");
                plus.setOnAction(e -> {
                    cart.updateQty(item, item.getQty() + 1);
                    refreshTotals();
                    getListView().refresh();
                });

                Button remove = new Button("Remove");
                remove.getStyleClass().add("btn-remove");
                remove.setOnAction(e -> { cart.removeItem(item); refreshTotals(); });

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(10, name, spacer, minus, qty, plus, price, remove);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 0, 6, 0));
                setGraphic(row);
            }
        };
    }

    private void refreshTotals() {
        subtotalLabel.setText(String.format("$%.0f", cart.getSubtotal()));
        shippingLabel.setText(cart.getShipping() == 0 ? "Free" : String.format("$%.0f", cart.getShipping()));
        totalLabel.setText(String.format("$%.0f", cart.getTotal()));
    }

    private void showError(String message) {
        orderError.setText(message);
        orderError.setVisible(true);
        orderError.setManaged(true);
    }

    private void hideError() {
        orderError.setVisible(false);
        orderError.setManaged(false);
    }

    private void resetForm() {
        formRevealed = false;
        checkoutForm.setVisible(false);
        checkoutForm.setManaged(false);
        shippingAddressField.clear();
        cardNumberField.clear();
        cardExpiryField.clear();
        cardCvvField.clear();
        confirmEmailField.clear();
        confirmPasswordField.clear();
        confirmButton.setText("Confirm Order");
    }

    @FXML
    private void confirmOrder() {
        if (!formRevealed) {
            if (cart.getItems().isEmpty()) {
                showError("Your cart is empty — add an item before confirming an order.");
                return;
            }

            hideError();
            formRevealed = true;
            checkoutForm.setVisible(true);
            checkoutForm.setManaged(true);
            confirmButton.setText("Place Order");
            return;
        }

        String address = shippingAddressField.getText() == null ? "" : shippingAddressField.getText().trim();
        if (address.isEmpty()) {
            showError("Please enter a shipping address.");
            return;
        }

        String cardNumber = cardNumberField.getText() == null ? "" : cardNumberField.getText().replaceAll("\\s", "");
        if (!CARD_PATTERN.matcher(cardNumber).matches()) {
            showError("Enter a valid card number (13 to 19 digits).");
            return;
        }

        String expiry = cardExpiryField.getText() == null ? "" : cardExpiryField.getText().trim();
        if (!EXPIRY_PATTERN.matcher(expiry).matches()) {
            showError("Enter the card expiry as MM/YY.");
            return;
        }

        String cvv = cardCvvField.getText() == null ? "" : cardCvvField.getText().trim();
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            showError("Enter a valid 3 or 4 digit CVV.");
            return;
        }

        String email = confirmEmailField.getText() == null ? "" : confirmEmailField.getText().trim();
        String password = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Enter your email and password to confirm.");
            return;
        }
        if (!email.equalsIgnoreCase(Session.getEmail()) || !userDao.checkCredentials(email, password)) {
            showError("That email and password don't match your logged-in account.");
            return;
        }

        String cardLast4 = cardNumber.substring(cardNumber.length() - 4);

        OrderResult result = OrderHistory.getInstance().createOrderFromCart(cart, address, cardLast4);
        if (!result.isSuccess()) {
            showError(result.getMessage());
            return;
        }

        hideError();
        resetForm();
        cart.clear();
        SceneManager.show("confirmed");
    }

    @FXML
    private void goBack() {
        SceneManager.show("dashboard");
    }
}
