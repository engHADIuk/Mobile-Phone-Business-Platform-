package Ecommerce.Products;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ProductFormController {

    @FXML private Label formTitle;
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private CheckBox cbCompact;
    @FXML private CheckBox cbFoldable;
    @FXML private CheckBox cbGaming;
    @FXML private CheckBox cbFlips;
    @FXML private TextArea descriptionField;
    @FXML private Spinner<Integer> priceSpinner;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private TextField imageUrlField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private Product editingProduct; // null = Add mode
    private Runnable onSaved;

    @FXML
    public void initialize() {
        priceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 0, 10));
        stockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 0, 1));
        statusCombo.getItems().addAll("Active", "Inactive");
        statusCombo.setValue("Active");
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    /** Pass null for Add mode, or an existing Product to pre-fill for Edit mode. */
    public void setProductToEdit(Product product) {
        this.editingProduct = product;
        if (product == null) return;

        formTitle.setText("Edit Product");
        saveButton.setText("Save Changes");
        idField.setText(product.getId());
        idField.setDisable(true); // ID cannot change once created
        nameField.setText(product.getName());
        descriptionField.setText(product.getDescription());
        priceSpinner.getValueFactory().setValue((int) Math.round(product.getPrice()));
        stockSpinner.getValueFactory().setValue(product.getStock());
        imageUrlField.setText(product.getImageUrl());
        statusCombo.setValue(product.getStatus());

        String cats = product.getCategories() == null ? "" : product.getCategories();
        cbCompact.setSelected(cats.contains("Compact"));
        cbFoldable.setSelected(cats.contains("Foldable"));
        cbGaming.setSelected(cats.contains("Gaming"));
        cbFlips.setSelected(cats.contains("Flips"));
    }

    @FXML
    private void handleSave() {
        hideError();

        String id = idField.getText() == null ? "" : idField.getText().trim();
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String imageUrl = imageUrlField.getText() == null ? "" : imageUrlField.getText().trim();
        int price = priceSpinner.getValue();
        int stock = stockSpinner.getValue();

        List<String> selectedCategories = new ArrayList<>();
        if (cbCompact.isSelected()) selectedCategories.add("Compact");
        if (cbFoldable.isSelected()) selectedCategories.add("Foldable");
        if (cbGaming.isSelected()) selectedCategories.add("Gaming");
        if (cbFlips.isSelected()) selectedCategories.add("Flips");

        // FR-03/FR-04 validation, matching the error-handling rules from the report
        if (id.isEmpty() || name.isEmpty() || description.isEmpty() || imageUrl.isEmpty()) {
            showError("All fields marked * are required.");
            return;
        }
        if (selectedCategories.isEmpty()) {
            showError("Select at least one category.");
            return;
        }
        if (price <= 0) {
            showError("Price must be a positive number.");
            return;
        }
        if (stock < 0) {
            showError("Stock cannot be negative.");
            return;
        }
        if (editingProduct == null && ProductDAO.idExists(id)) {
            showError("Product ID " + id + " already exists.");
            return;
        }

        Product product = new Product(id, name, description, price, stock, imageUrl, statusCombo.getValue(), null);

        try {
            if (editingProduct == null) {
                ProductDAO.addProduct(product, selectedCategories);
            } else {
                ProductDAO.updateProduct(product, selectedCategories);
            }
            if (onSaved != null) onSaved.run();
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}