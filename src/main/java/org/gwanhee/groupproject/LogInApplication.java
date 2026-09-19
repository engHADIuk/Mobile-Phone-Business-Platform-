package org.gwanhee.groupproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LogInApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(LogInApplication.class.getResource("LogIn.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }
}
