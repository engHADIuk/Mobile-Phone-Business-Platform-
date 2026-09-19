package com.nc1605.inventorywarehouse.controller;
import com.nc1605.inventorywarehouse.App; import com.nc1605.inventorywarehouse.data.Database; import com.nc1605.inventorywarehouse.model.InventoryItem;
import javafx.beans.property.SimpleIntegerProperty; import javafx.beans.property.SimpleStringProperty; import javafx.collections.FXCollections; import javafx.fxml.FXML; import javafx.scene.control.*; import javafx.scene.layout.HBox; import javafx.scene.shape.Rectangle;
public class InventoryController extends BaseController {
    @FXML private TextField searchField; @FXML private ComboBox<String> statusBox; @FXML private TableView<InventoryItem> inventoryTable; @FXML private Label warehouseFilterLabel; @FXML private HBox warehouseFilterBar; @FXML private Button backButton; @FXML private Button clearFilterButton;
    private String warehouseFilter, returnScreen;
    @FXML public void initialize(){ statusBox.setItems(FXCollections.observableArrayList("All","In Stock","Low Stock","Critical"));statusBox.setValue(App.takeInventoryFilter());
        warehouseFilter=App.takeWarehouseFilter(); returnScreen=App.takeReturnScreen(); boolean drillDown=!returnScreen.isBlank(); warehouseFilterBar.setVisible(drillDown); warehouseFilterBar.setManaged(drillDown); backButton.setText(drillDown ? "← Back to " + returnScreen : "← Back"); warehouseFilterLabel.setText(warehouseFilter.isBlank() ? ("All".equals(statusBox.getValue()) ? "Showing all inventory" : "Filtered by " + statusBox.getValue()) : "Showing stock at " + warehouseFilter); clearFilterButton.setVisible(!warehouseFilter.isBlank() || !"All".equals(statusBox.getValue())); clearFilterButton.setManaged(clearFilterButton.isVisible());
        stringColumn("Product", i -> i.getProductName()); stringColumn("Warehouse", i -> i.getWarehouseName()); integerColumn("Quantity", InventoryItem::getQuantity); integerColumn("Minimum Stock", InventoryItem::getThreshold); statusColumn(); applyRoundedClip(); refresh(); }
    private void applyRoundedClip(){Rectangle clip=new Rectangle();clip.widthProperty().bind(inventoryTable.widthProperty());clip.heightProperty().bind(inventoryTable.heightProperty());clip.setArcWidth(32);clip.setArcHeight(32);inventoryTable.setClip(clip);}
    private void stringColumn(String title, java.util.function.Function<InventoryItem,String> value){TableColumn<InventoryItem,String> c=new TableColumn<>(title);c.setCellValueFactory(x->new SimpleStringProperty(value.apply(x.getValue())));inventoryTable.getColumns().add(c);}
    private void integerColumn(String title, java.util.function.ToIntFunction<InventoryItem> value){TableColumn<InventoryItem,Integer> c=new TableColumn<>(title);c.setCellValueFactory(x->new SimpleIntegerProperty(value.applyAsInt(x.getValue())).asObject());inventoryTable.getColumns().add(c);}
    private void statusColumn(){TableColumn<InventoryItem,String> c=new TableColumn<>("Status");c.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getStatus()));c.setCellFactory(column->new TableCell<>(){@Override protected void updateItem(String status, boolean empty){super.updateItem(status,empty);getStyleClass().removeAll("status-good","status-low","status-critical");if(empty||status==null){setText(null);}else{setText(status);getStyleClass().add(status.equals("In Stock")?"status-good":status.equals("Low Stock")?"status-low":"status-critical");}}});inventoryTable.getColumns().add(c);}
    @FXML private void refresh(){
        java.util.List<InventoryItem> items=Database.inventory(searchField==null?"":searchField.getText().trim(),statusBox==null?"All":statusBox.getValue());
        if(!warehouseFilter.isBlank()) items=items.stream().filter(item->item.getWarehouseName().equals(warehouseFilter)).toList();
        inventoryTable.setItems(FXCollections.observableArrayList(items));
        inventoryTable.setPrefHeight(Math.min(432, 58 + inventoryTable.getItems().size() * 40));
    }
    @FXML private void clearWarehouseFilter(){warehouseFilter="";statusBox.setValue("All");warehouseFilterLabel.setText("Showing all inventory");clearFilterButton.setVisible(false);clearFilterButton.setManaged(false);refresh();}
    @FXML private void backToSource() throws java.io.IOException {if(!returnScreen.isBlank()) App.show(returnScreen);}
}
