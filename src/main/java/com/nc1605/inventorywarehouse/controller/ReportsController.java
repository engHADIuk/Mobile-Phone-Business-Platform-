package com.nc1605.inventorywarehouse.controller;
import com.nc1605.inventorywarehouse.data.Database; import com.nc1605.inventorywarehouse.model.InventoryItem;
import javafx.collections.FXCollections; import javafx.collections.ObservableList; import javafx.fxml.FXML; import javafx.scene.chart.*;
public class ReportsController extends BaseController {
    @FXML private BarChart<String,Number> stockChart;
    @FXML public void initialize(){XYChart.Series<String,Number> series=new XYChart.Series<>(); for(InventoryItem i:Database.inventory("","All"))series.getData().add(new XYChart.Data<>(i.getProductName()+"\n"+i.getWarehouseName(),i.getQuantity()));ObservableList<XYChart.Series<String,Number>> chartData=FXCollections.observableArrayList();chartData.add(series);stockChart.setData(chartData);}
}
