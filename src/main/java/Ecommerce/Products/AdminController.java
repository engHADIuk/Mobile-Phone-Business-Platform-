package Ecommerce.Products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class AdminController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colImage;
    @FXML private TableColumn<Product, String> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategories;
    @FXML private TableColumn<Product, Number> colPrice;
    @FXML private TableColumn<Product, Number> colStock;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Void> colActions;
    @FXML private Label emptyStateLabel;

    private final ObservableList<Product> data = FXCollections.observableArrayList();
    private double dragOffsetX, dragOffsetY;

    @FXML
    public void initialize() {
        categoryFilter.setItems(FXCollections.observableArrayList(ProductDAO.getAllCategoryNames()));
        categoryFilter.getItems().add(0, "All Categories");
        categoryFilter.setValue("All Categories");

        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.setValue("All");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategories.setCellValueFactory(new PropertyValueFactory<>("categories"));
        colStock.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getStock()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPrice.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getPrice()));
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("£%.2f", value.doubleValue()));
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(value);
                badge.getStyleClass().add("Active".equalsIgnoreCase(value) ? "status-badge-active" : "status-badge-inactive");
                setGraphic(badge);
                setText(null);
            }
        });

        colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            @Override protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) { setGraphic(null); return; }
                try {
                    iv.setImage(new Image(url, 40, 40, true, true, true));
                    iv.setFitWidth(40);
                    iv.setFitHeight(40);
                    setGraphic(iv);
                } catch (Exception e) {
                    setGraphic(null);
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(10, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("link-button");
                deleteBtn.getStyleClass().add("link-button-danger");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void value, boolean empty) {
                super.updateItem(value, empty);
                setGraphic(empty ? null : box);
            }
        });

        productTable.setItems(data);
        loadAll();
    }

    private void loadAll() {
        data.setAll(ProductDAO.getAllProducts());
        emptyStateLabel.setVisible(data.isEmpty());
        emptyStateLabel.setManaged(data.isEmpty());
    }

    @FXML
    private void handleSearch() {
        List<Product> results = ProductDAO.search(searchField.getText(), categoryFilter.getValue(), statusFilter.getValue());
        data.setAll(results);
        emptyStateLabel.setVisible(data.isEmpty());
        emptyStateLabel.setManaged(data.isEmpty());
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        statusFilter.setValue("All");
        loadAll();
    }

    @FXML
    private void handleAddProduct() {
        openProductForm(null);
    }

    private void handleEdit(Product product) {
        openProductForm(product);
    }

    private HBox spacer() {
        HBox box = new HBox();
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /** Wraps arbitrary content in a borderless, draggable, Escape-closable modal with a close button. */
    private Stage createModal(String titleText, javafx.scene.Node body, double width) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label title = new Label(titleText);
        title.getStyleClass().add("form-title");

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("modal-close-button");
        closeBtn.setOnAction(e -> dialog.close());

        HBox headerRow = new HBox(title, spacer(), closeBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("modal-header-row");

        VBox root = new VBox(14, headerRow, body);
        root.getStyleClass().addAll("root-bg", "form-container");
        root.setPadding(new Insets(24));
        root.setPrefWidth(width);
        root.setOpacity(0);
        root.setScaleX(0.94);
        root.setScaleY(0.94);

        headerRow.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        headerRow.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - dragOffsetX);
            dialog.setY(e.getScreenY() - dragOffsetY);
        });

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(scrollPane);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) dialog.close();
        });
        dialog.setScene(scene);

        dialog.setOnShown(e -> {
            dialog.centerOnScreen();
            FadeTransition fade = new FadeTransition(Duration.millis(220), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            ScaleTransition scale = new ScaleTransition(Duration.millis(220), root);
            scale.setFromX(0.94);
            scale.setFromY(0.94);
            scale.setToX(1);
            scale.setToY(1);
            new ParallelTransition(fade, scale).play();
        });

        return dialog;
    }

    private void openProductForm(Product productOrNull) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("product_form.fxml"));
            Parent formRoot = loader.load();
            ProductFormController controller = loader.getController();
            controller.setOnSaved(this::loadAll);

            Stage dialog = new Stage();
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.initModality(Modality.APPLICATION_MODAL);

            Label title = new Label(productOrNull == null ? "Add New Product" : "Edit Product");
            title.getStyleClass().add("form-title");
            Button closeBtn = new Button("✕");
            closeBtn.getStyleClass().add("modal-close-button");
            closeBtn.setOnAction(e -> dialog.close());
            HBox headerRow = new HBox(title, spacer(), closeBtn);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.getStyleClass().add("modal-header-row");
            headerRow.setOnMousePressed(e -> {
                dragOffsetX = e.getSceneX();
                dragOffsetY = e.getSceneY();
            });
            headerRow.setOnMouseDragged(e -> {
                dialog.setX(e.getScreenX() - dragOffsetX);
                dialog.setY(e.getScreenY() - dragOffsetY);
            });

            VBox wrapper = new VBox(10, headerRow, formRoot);
            wrapper.getStyleClass().addAll("root-bg", "form-container");
            wrapper.setPadding(new Insets(20));
            wrapper.setOpacity(0);
            wrapper.setScaleX(0.94);
            wrapper.setScaleY(0.94);

            controller.setProductToEdit(productOrNull);

            ScrollPane scrollPane = new ScrollPane(wrapper);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(scrollPane);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) dialog.close();
            });
            dialog.setScene(scene);

            dialog.setOnShown(e -> {
                dialog.centerOnScreen();
                FadeTransition fade = new FadeTransition(Duration.millis(220), wrapper);
                fade.setFromValue(0);
                fade.setToValue(1);
                ScaleTransition scale = new ScaleTransition(Duration.millis(220), wrapper);
                scale.setFromX(0.94);
                scale.setFromY(0.94);
                scale.setToX(1);
                scale.setToY(1);
                new ParallelTransition(fade, scale).play();
            });

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not open the product form: " + e.getMessage()).showAndWait();
        }
    }

    private void handleDelete(Product product) {
        Label warning = new Label("This action cannot be undone.\nType the product name and ID exactly to confirm:");
        warning.setWrapText(true);
        warning.getStyleClass().add("subtitle-muted");

        TextField nameField = new TextField();
        nameField.setPromptText("Product name");
        nameField.getStyleClass().add("text-input");
        TextField idField = new TextField();
        idField.setPromptText("Product ID");
        idField.getStyleClass().add("text-input");

        Button confirmBtn = new Button("Delete Permanently");
        confirmBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");

        HBox actionRow = new HBox(12, cancelBtn, spacer(), confirmBtn);

        VBox body = new VBox(12, warning, nameField, idField, actionRow);

        Stage dialog = createModal("Confirm Deletion", body, 420);
        cancelBtn.setOnAction(e -> dialog.close());
        confirmBtn.setOnAction(e -> {
            if (nameField.getText().equals(product.getName()) && idField.getText().equals(product.getId())) {
                try {
                    ProductDAO.deleteProduct(product.getId());
                    loadAll();
                    dialog.close();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Delete failed: " + ex.getMessage()).showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Name or ID did not match. Product was not deleted.").showAndWait();
            }
        });
        dialog.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            App.setRoot("login", 900, 620);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}