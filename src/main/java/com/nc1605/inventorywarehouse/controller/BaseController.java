package com.nc1605.inventorywarehouse.controller;
import com.nc1605.inventorywarehouse.App;
import javafx.fxml.FXML;
import java.io.IOException;
public abstract class BaseController {
    @FXML protected void dashboard() throws IOException { App.show("Dashboard"); }
    @FXML protected void inventory() throws IOException { App.showInventory("All"); }
    @FXML protected void products() throws IOException { App.show("Products"); }
    @FXML protected void warehouses() throws IOException { App.show("Warehouses"); }
    @FXML protected void transfers() throws IOException { App.show("Transfers"); }
    @FXML protected void reports() throws IOException { App.show("Reports"); }
}
