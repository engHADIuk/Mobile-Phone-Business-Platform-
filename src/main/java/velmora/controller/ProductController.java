package velmora.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import velmora.SceneManager;
import velmora.db.CatalogDao;
import velmora.db.ProductDao;
import velmora.model.Cart;
import velmora.model.Product;
import velmora.model.StorageOption;

public class ProductController implements Initializable {

    @FXML private Label productNameLabel;
    @FXML private Label productDescLabel;
    @FXML private Label priceLabel;
    @FXML private Label oldPriceLabel;
    @FXML private Label qtyLabel;
    @FXML private Label qtyError;
    @FXML private Label stockTag;

    @FXML private ToggleButton colorBlack, colorSilver, colorBlue;
    @FXML private ToggleButton storage128, storage256, storage512;

    private Product product;
    private int qty = 1;
    private Map<String, StorageOption> storageOptions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CatalogDao.Info info = new CatalogDao().find();
        productNameLabel.setText(info.name);
        productDescLabel.setText(info.description);

        product = new Product(info.name, "Obsidian Black", "128GB", 899, 1099, 16);

        storageOptions = new HashMap<>();
        for (StorageOption option : new ProductDao().findAll()) {
            storageOptions.put(option.getStorage(), option);
        }

        applyStorage(storageOptions.get("128GB"));
    }

    @FXML
    private void selectColor() {
        if (colorBlack.isSelected()) product.setColor("Obsidian Black");
        else if (colorSilver.isSelected()) product.setColor("Titanium Silver");
        else if (colorBlue.isSelected()) product.setColor("Midnight Blue");
    }

    @FXML
    private void selectStorage() {
        if (storage128.isSelected())      applyStorage(storageOptions.get("128GB"));
        else if (storage256.isSelected()) applyStorage(storageOptions.get("256GB"));
        else if (storage512.isSelected()) applyStorage(storageOptions.get("512GB"));
    }

    private void applyStorage(StorageOption option) {
        product.setStorage(option.getStorage());
        product.setPrice(option.getPrice());
        product.setOldPrice(option.getOldPrice());
        product.setStock(option.getStock());
        priceLabel.setText(String.format("$%.0f", option.getPrice()));
        oldPriceLabel.setText(String.format("$%.0f", option.getOldPrice()));
        stockTag.setText(option.getStock() + " available now");
        qty = 1;
        qtyLabel.setText("1");
        qtyError.setVisible(false);
        qtyError.setManaged(false);
    }

    @FXML
    private void increaseQty() {
        if (qty < product.getStock() && qty < 10) {
            qty++;
            qtyLabel.setText(String.valueOf(qty));
            qtyError.setVisible(false);
            qtyError.setManaged(false);
        } else {
            qtyError.setVisible(true);
            qtyError.setManaged(true);
        }
    }

    @FXML
    private void decreaseQty() {
        if (qty > 1) {
            qty--;
            qtyLabel.setText(String.valueOf(qty));
        }
    }

    @FXML
    private void addToCart() {
        Cart.getInstance().addItem(
                new Product(product.getName(), product.getColor(), product.getStorage(),
                        product.getPrice(), product.getOldPrice(), product.getStock()),
                qty);
        SceneManager.show("cart");
    }

    @FXML
    private void buyNow() {
        addToCart();
    }

    @FXML
    private void goBack() {
        SceneManager.show("dashboard");
    }
}
