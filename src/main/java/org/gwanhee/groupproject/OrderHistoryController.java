package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.awt.*;
import java.io.IOException;

public class OrderHistoryController {

    @FXML
    private Button backButton;

    public void backbutton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Main.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
