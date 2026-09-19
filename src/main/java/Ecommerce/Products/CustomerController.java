package Ecommerce.Products;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class CustomerController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> availabilityFilter;
    @FXML private Label resultCountLabel;
    @FXML private Label favouritesBadge;
    @FXML private Label cartBadge;
    @FXML private FlowPane productGrid;

    private List<Product> allActiveProducts = new ArrayList<>();
    private final Set<String> favouriteIds = new LinkedHashSet<>();
    private final Map<String, Integer> basket = new LinkedHashMap<>();

    // drag-tracking for borderless modals
    private double dragOffsetX, dragOffsetY;

    @FXML
    public void initialize() {
        List<String> cats = new ArrayList<>(ProductDAO.getAllCategoryNames());
        cats.add(0, "All Categories");
        categoryFilter.setItems(FXCollections.observableArrayList(cats));
        categoryFilter.setValue("All Categories");

        priceFilter.setItems(FXCollections.observableArrayList(
            "All Prices", "Under £500", "£500 - £1000", "£1000 - £1500", "Over £1500"));
        priceFilter.setValue("All Prices");

        availabilityFilter.setItems(FXCollections.observableArrayList("All Availability", "In Stock", "Out of Stock"));
        availabilityFilter.setValue("All Availability");

        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
        priceFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
        availabilityFilter.valueProperty().addListener((obs, old, val) -> applyFilters());

        loadProducts();
    }

    private void loadProducts() {
        allActiveProducts = ProductDAO.search(null, null, "Active");
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String category = categoryFilter.getValue();
        String priceRange = priceFilter.getValue();
        String availability = availabilityFilter.getValue();

        List<Product> results = new ArrayList<>();
        for (Product p : allActiveProducts) {
            if (!keyword.isEmpty() &&
                !p.getName().toLowerCase().contains(keyword) &&
                !p.getId().toLowerCase().contains(keyword)) continue;

            if (category != null && !category.equals("All Categories") &&
                (p.getCategories() == null || !p.getCategories().contains(category))) continue;

            if (priceRange != null && !priceRange.equals("All Prices")) {
                double price = p.getPrice();
                boolean match = switch (priceRange) {
                    case "Under £500" -> price < 500;
                    case "£500 - £1000" -> price >= 500 && price <= 1000;
                    case "£1000 - £1500" -> price > 1000 && price <= 1500;
                    case "Over £1500" -> price > 1500;
                    default -> true;
                };
                if (!match) continue;
            }

            if (availability != null && !availability.equals("All Availability")) {
                boolean wantInStock = availability.equals("In Stock");
                if (wantInStock == p.isOutOfStock()) continue;
            }

            results.add(p);
        }

        renderGrid(results);
        resultCountLabel.setText(results.size() + " result" + (results.size() == 1 ? "" : "s"));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        priceFilter.setValue("All Prices");
        availabilityFilter.setValue("All Availability");
        applyFilters();
    }

    private void renderGrid(List<Product> products) {
        productGrid.getChildren().clear();
        productGrid.setOpacity(0);

        if (products.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            Label icon = new Label("🔍");
            icon.getStyleClass().add("empty-state-icon");
            Label text = new Label("No products found.");
            text.getStyleClass().add("empty-state");
            empty.getChildren().addAll(icon, text);
            productGrid.getChildren().add(empty);
        } else {
            int delayStep = 40;
            int i = 0;
            for (Product p : products) {
                VBox card = buildProductCard(p);
                card.setOpacity(0);
                card.setTranslateY(16);
                productGrid.getChildren().add(card);
                animateCardIn(card, i * delayStep);
                i++;
            }
        }

        FadeTransition gridFade = new FadeTransition(Duration.millis(280), productGrid);
        gridFade.setFromValue(0);
        gridFade.setToValue(1);
        gridFade.play();
    }

    private void animateCardIn(VBox card, int delayMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(320), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMillis));

        TranslateTransition slide = new TranslateTransition(Duration.millis(320), card);
        slide.setFromY(16);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMillis));

        new ParallelTransition(fade, slide).play();
    }

    private void playPop(javafx.scene.Node node) {
        ScaleTransition pop = new ScaleTransition(Duration.millis(90), node);
        pop.setFromX(1);
        pop.setFromY(1);
        pop.setToX(1.25);
        pop.setToY(1.25);
        pop.setCycleCount(2);
        pop.setAutoReverse(true);
        pop.play();
    }

    private String primaryCategory(Product p) {
        if (p.getCategories() == null || p.getCategories().isBlank()) return "Default";
        return p.getCategories().split(",\\s*")[0];
    }

    private String gradientClassFor(String category) {
        return switch (category) {
            case "Smartphones" -> "gradient-smartphones";
            case "Laptops" -> "gradient-laptops";
            case "Audio" -> "gradient-audio";
            case "Wearables" -> "gradient-wearables";
            case "Accessories" -> "gradient-accessories";
            default -> "gradient-default";
        };
    }

    private String pillClassFor(String category) {
        return switch (category) {
            case "Smartphones" -> "pill-smartphones";
            case "Laptops" -> "pill-laptops";
            case "Audio" -> "pill-audio";
            case "Wearables" -> "pill-wearables";
            case "Accessories" -> "pill-accessories";
            default -> "category-pill";
        };
    }

    private VBox buildProductCard(Product p) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(260);
        card.setMaxWidth(260);

        StackPane imageWrap = new StackPane();
        imageWrap.getStyleClass().addAll("card-image-wrap", gradientClassFor(primaryCategory(p)));
        imageWrap.setPrefHeight(180);

        Label initial = new Label(p.getName() != null && !p.getName().isBlank()
            ? String.valueOf(p.getName().charAt(0)).toUpperCase() : "V");
        initial.getStyleClass().add("card-initial");

        ImageView iv = new ImageView();
        iv.setFitWidth(260);
        iv.setFitHeight(180);
        iv.setPreserveRatio(false);
        iv.setVisible(false);
        try {
            Image img = new Image(p.getImageUrl(), 260, 180, false, true, true);
            iv.setImage(img);
            img.errorProperty().addListener((obs, was, isNow) -> iv.setVisible(!isNow));
            if (!img.isError()) iv.setVisible(true);
        } catch (Exception ignored) { }

        ToggleButton heart = new ToggleButton(favouriteIds.contains(p.getId()) ? "♥" : "♡");
        heart.getStyleClass().add("heart-toggle");
        heart.setSelected(favouriteIds.contains(p.getId()));
        StackPane.setAlignment(heart, Pos.TOP_RIGHT);
        StackPane.setMargin(heart, new Insets(10));
        heart.setOnAction(e -> {
            toggleFavourite(p, heart);
            playPop(heart);
        });

        HBox pillBox = new HBox(6);
        pillBox.setPadding(new Insets(0, 0, 10, 10));
        StackPane.setAlignment(pillBox, Pos.BOTTOM_LEFT);
        if (p.getCategories() != null && !p.getCategories().isBlank()) {
            for (String cat : p.getCategories().split(",\\s*")) {
                Label pill = new Label(cat);
                pill.getStyleClass().add(pillClassFor(cat));
                pillBox.getChildren().add(pill);
            }
        }

        imageWrap.getChildren().addAll(initial, iv, heart, pillBox);

        if (p.isOutOfStock()) {
            Label oosBadge = new Label("Out of Stock");
            oosBadge.getStyleClass().add("out-of-stock-badge");
            StackPane.setAlignment(oosBadge, Pos.TOP_LEFT);
            StackPane.setMargin(oosBadge, new Insets(10));
            imageWrap.getChildren().add(oosBadge);
        }

        Label name = new Label(p.getName());
        name.getStyleClass().add("card-title");

        Label stock = new Label(p.isOutOfStock() ? "Out of stock" : p.getStock() + " in stock");
        stock.getStyleClass().add(p.isOutOfStock() ? "card-stock-out" : "card-stock");

        Label priceLabel = new Label(p.getPriceFormatted());
        priceLabel.getStyleClass().add("card-price");

        Button cartBtn = new Button("🛒");
        cartBtn.getStyleClass().add("cart-quick-add");
        cartBtn.setDisable(p.isOutOfStock());
        cartBtn.setOnAction(e -> {
            addToBasket(p, 1);
            playPop(cartBtn);
        });

        HBox priceRow = new HBox(priceLabel, spacer(), cartBtn);
        priceRow.setAlignment(Pos.CENTER_LEFT);

        VBox textBlock = new VBox(6, name, stock, priceRow);
        textBlock.getStyleClass().add("card-text-block");
        textBlock.setPadding(new Insets(14));

        card.getChildren().addAll(imageWrap, textBlock);
        card.setOnMouseClicked(e -> {
            if (e.getTarget() == heart || e.getTarget() == cartBtn) return;
            showProductDetail(p);
        });

        return card;
    }

    private HBox spacer() {
        HBox box = new HBox();
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /** Small gradient swatch with initial letter, used in Basket/Favourites rows. */
    private StackPane buildMiniSwatch(Product p) {
        StackPane swatch = new StackPane();
        swatch.getStyleClass().addAll("card-image-wrap", gradientClassFor(primaryCategory(p)));
        swatch.setPrefSize(44, 44);
        swatch.setMaxSize(44, 44);
        swatch.setMinSize(44, 44);
        Label initial = new Label(p.getName() != null && !p.getName().isBlank()
            ? String.valueOf(p.getName().charAt(0)).toUpperCase() : "V");
        initial.getStyleClass().add("mini-swatch-initial");
        swatch.getChildren().add(initial);
        return swatch;
    }

    private void toggleFavourite(Product p, ToggleButton heart) {
        if (favouriteIds.contains(p.getId())) {
            favouriteIds.remove(p.getId());
            heart.setText("♡");
        } else {
            favouriteIds.add(p.getId());
            heart.setText("♥");
        }
        updateFavouritesBadge();
    }

    private void addToBasket(Product p, int qty) {
        if (p.isOutOfStock()) return;
        basket.merge(p.getId(), qty, Integer::sum);
        updateCartBadge();
    }

    private void updateFavouritesBadge() {
        favouritesBadge.setText(String.valueOf(favouriteIds.size()));
        favouritesBadge.setVisible(!favouriteIds.isEmpty());
    }

    private void updateCartBadge() {
        int total = basket.values().stream().mapToInt(Integer::intValue).sum();
        cartBadge.setText(String.valueOf(total));
        cartBadge.setVisible(total > 0);
    }

    /**
     * Builds a borderless, draggable, Escape-closable modal Stage with a close button
     * in its header, wrapping the given body content. Fades + scales in on show.
     */
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

        // drag support via header row (borderless windows can't be dragged by OS default)
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

    private void showProductDetail(Product p) {
        StackPane heroWrap = new StackPane();
        heroWrap.getStyleClass().addAll("card-image-wrap", gradientClassFor(primaryCategory(p)));
        heroWrap.setPrefSize(500, 260);
        Label heroInitial = new Label(p.getName() != null && !p.getName().isBlank()
            ? String.valueOf(p.getName().charAt(0)).toUpperCase() : "V");
        heroInitial.getStyleClass().add("card-initial");

        ImageView iv = new ImageView();
        iv.setFitWidth(500);
        iv.setFitHeight(260);
        iv.setVisible(false);
        try {
            Image img = new Image(p.getImageUrl(), 500, 260, false, true, true);
            iv.setImage(img);
            img.errorProperty().addListener((obs, was, isNow) -> iv.setVisible(!isNow));
            if (!img.isError()) iv.setVisible(true);
        } catch (Exception ignored) { }
        heroWrap.getChildren().addAll(heroInitial, iv);

        Label name = new Label(p.getName());
        name.getStyleClass().add("form-title");
        Label desc = new Label(p.getDescription());
        desc.setWrapText(true);
        desc.getStyleClass().add("subtitle-muted");
        Label price = new Label(p.getPriceFormatted());
        price.getStyleClass().add("card-price");
        Label stock = new Label(p.isOutOfStock() ? "Out of stock" : p.getStock() + " in stock");
        stock.getStyleClass().add(p.isOutOfStock() ? "card-stock-out" : "card-stock");

        Hyperlink moreInfoLink = new Hyperlink("Additional Information");
        Label moreInfo = new Label("Warranty: 12 months  •  Release year: 2025");
        moreInfo.setVisible(false);
        moreInfo.setManaged(false);
        moreInfoLink.setOnAction(e -> {
            moreInfo.setVisible(!moreInfo.isVisible());
            moreInfo.setManaged(!moreInfo.isManaged());
        });

        Spinner<Integer> qtySpinner = new Spinner<>(1, Math.max(1, p.getStock()), 1);
        qtySpinner.setEditable(true);
        qtySpinner.setDisable(p.isOutOfStock());

        Button addBtn = new Button("Add to Basket");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setDisable(p.isOutOfStock());

        ToggleButton heart = new ToggleButton(favouriteIds.contains(p.getId()) ? "♥ Saved" : "♡ Save to Favourites");
        heart.getStyleClass().add("secondary-button");
        heart.setSelected(favouriteIds.contains(p.getId()));
        heart.setOnAction(e -> {
            if (favouriteIds.contains(p.getId())) {
                favouriteIds.remove(p.getId());
                heart.setText("♡ Save to Favourites");
            } else {
                favouriteIds.add(p.getId());
                heart.setText("♥ Saved");
            }
            updateFavouritesBadge();
            playPop(heart);
        });

        HBox actionRow = new HBox(12, qtySpinner, addBtn, heart);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(14, heroWrap, name, desc, price, stock, moreInfoLink, moreInfo, actionRow);

        Stage dialog = createModal(p.getName(), body, 560);
        addBtn.setOnAction(e -> {
            addToBasket(p, qtySpinner.getValue());
            playPop(addBtn);
            dialog.close();
        });
        dialog.showAndWait();
    }

    @FXML
    private void handleOpenBasket() {
        VBox listBox = new VBox(10);

        if (basket.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(20, 0, 10, 0));
            Label icon = new Label("🛒");
            icon.getStyleClass().add("empty-state-icon");
            Label text = new Label("Your basket is empty.");
            text.getStyleClass().add("empty-state");
            Hyperlink browseLink = new Hyperlink("Browse products");
            browseLink.getStyleClass().add("browse-link");
            empty.getChildren().addAll(icon, text, browseLink);
            listBox.getChildren().add(empty);

            Stage dialog = createModal("Shopping Basket", listBox, 480);
            browseLink.setOnAction(e -> dialog.close());
            dialog.showAndWait();
            return;
        }

        double total = 0;
        for (Map.Entry<String, Integer> entry : basket.entrySet()) {
            Product p = findById(entry.getKey());
            if (p == null) continue;
            double lineTotal = p.getPrice() * entry.getValue();
            total += lineTotal;

            StackPane swatch = buildMiniSwatch(p);
            Label line = new Label(p.getName() + "  x" + entry.getValue() + "   —   £" + String.format("%.2f", lineTotal));
            line.getStyleClass().add("card-stock");
            Button remove = new Button("Remove");
            remove.getStyleClass().add("link-button-danger");
            remove.setOnAction(e -> {
                basket.remove(entry.getKey());
                updateCartBadge();
                handleOpenBasket();
            });
            HBox row = new HBox(12, swatch, line, spacer(), remove);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("basket-line-row");
            listBox.getChildren().add(row);
        }

        Label totalLabel = new Label("Total: £" + String.format("%.2f", total));
        totalLabel.getStyleClass().add("card-price");

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("primary-button");

        Button continueBtn = new Button("Continue Shopping");
        continueBtn.getStyleClass().add("secondary-button");

        HBox actionRow = new HBox(12, continueBtn, spacer(), checkoutBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(14, listBox, totalLabel, actionRow);

        Stage dialog = createModal("Shopping Basket", body, 480);
        continueBtn.setOnAction(e -> dialog.close());
        checkoutBtn.setOnAction(e -> {
            handleCheckout();
            dialog.close();
        });
        dialog.showAndWait();
    }

    private void handleCheckout() {
        for (Map.Entry<String, Integer> entry : basket.entrySet()) {
            Product current = ProductDAO.search(entry.getKey(), null, null)
                    .stream().filter(p -> p.getId().equals(entry.getKey())).findFirst().orElse(null);
            if (current == null || current.getStock() < entry.getValue()) {
                new Alert(Alert.AlertType.ERROR,
                    "Not enough stock for " + (current == null ? entry.getKey() : current.getName()) +
                    ". Please adjust your basket.").showAndWait();
                return;
            }
        }
        basket.clear();
        updateCartBadge();
        new Alert(Alert.AlertType.INFORMATION, "Order placed successfully!").showAndWait();
    }

    @FXML
    private void handleOpenFavourites() {
        VBox listBox = new VBox(10);

        if (favouriteIds.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(20, 0, 10, 0));
            Label icon = new Label("♡");
            icon.getStyleClass().add("empty-state-icon");
            Label text = new Label("You haven't saved any favourites yet.");
            text.getStyleClass().add("empty-state");
            Hyperlink browseLink = new Hyperlink("Browse products");
            browseLink.getStyleClass().add("browse-link");
            empty.getChildren().addAll(icon, text, browseLink);
            listBox.getChildren().add(empty);

            Stage dialog = createModal("Favourites", listBox, 480);
            browseLink.setOnAction(e -> dialog.close());
            dialog.showAndWait();
            return;
        }

        for (String id : favouriteIds) {
            Product p = findById(id);
            if (p == null) continue;
            StackPane swatch = buildMiniSwatch(p);
            Label line = new Label(p.getName() + "   —   " + p.getPriceFormatted());
            line.getStyleClass().add("card-stock");
            Button addBtn = new Button("Add to Basket");
            addBtn.getStyleClass().add("secondary-button");
            addBtn.setDisable(p.isOutOfStock());
            addBtn.setOnAction(e -> addToBasket(p, 1));
            HBox row = new HBox(12, swatch, line, spacer(), addBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("basket-line-row");
            listBox.getChildren().add(row);
        }

        Button continueBtn = new Button("Continue Shopping");
        continueBtn.getStyleClass().add("secondary-button");
        HBox actionRow = new HBox(continueBtn);

        VBox body = new VBox(14, listBox, actionRow);

        Stage dialog = createModal("Favourites", body, 480);
        continueBtn.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private Product findById(String id) {
        return allActiveProducts.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
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