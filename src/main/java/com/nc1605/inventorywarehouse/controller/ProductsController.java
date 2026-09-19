package com.nc1605.inventorywarehouse.controller;

import com.nc1605.inventorywarehouse.data.Database;
import com.nc1605.inventorywarehouse.model.ProductStock;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Displays the four VELMORA product lines and their combined available stock. */
public class ProductsController extends BaseController {
    @FXML private FlowPane productCards;

    @FXML public void initialize() {
        for (ProductStock product : Database.products()) productCards.getChildren().add(createCard(product));
    }

    private VBox createCard(ProductStock product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");

        String file = product.getName().contains("Compact") ? "velmora-compact.jpg"
                : product.getName().contains("Fold") ? "velmora-fold.jpg"
                : product.getName().contains("Gaming") ? "velmora-gaming.jpg"
                : "velmora-flip.jpg";
        var resource = getClass().getResource("/com/nc1605/inventorywarehouse/images/" + file);
        StackPane imageHolder = new StackPane();
        imageHolder.getStyleClass().add("product-image-holder");
        if (resource != null) {
            ImageView image = new ImageView(new Image(resource.toExternalForm(), 150, 125, true, true));
            image.setFitWidth(150);
            image.setFitHeight(125);
            image.setPreserveRatio(true);
            imageHolder.getChildren().add(image);
        }

        Label name = new Label(product.getName());
        name.getStyleClass().add("product-name");
        Label stock = new Label(product.getTotalStock() + " units left");
        stock.getStyleClass().add("stock-count");
        card.getChildren().addAll(imageHolder, name, stock);
        return card;
    }
}
