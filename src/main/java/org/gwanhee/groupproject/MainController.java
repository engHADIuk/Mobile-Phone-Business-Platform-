package org.gwanhee.groupproject;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML
    private VBox sideBar;

    private boolean sidebarOpen = false;

    @FXML
    private void initialize() {
        sideBar.setTranslateX(200);
        sideBar.setVisible(false);
        sideBar.setManaged(false);
    }

    @FXML
    private void toggleSidebar() {
        if (sidebarOpen) {
            closeSidebar();
        } else {
            openSidebar();
        }
    }

    private void openSidebar() {
        sideBar.setVisible(true);
        sideBar.setManaged(true);

        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sideBar);
        transition.setFromX(200);
        transition.setToX(0);
        transition.play();

        sidebarOpen = true;
    }

    private void closeSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sideBar);
        transition.setFromX(0);
        transition.setToX(200);

        transition.setOnFinished(event -> {
            sideBar.setVisible(false);
            sideBar.setManaged(false);
        });

        transition.play();

        sidebarOpen = false;
    }

    @FXML
    private void logoutfunction() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirm");
        alert.setHeaderText("Confirm Logout?");
        alert.setContentText("If you press Yes, you will be logged out.");
        ButtonType btnYes = new ButtonType("Yes");
        ButtonType btnNo = new ButtonType("No");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(btnNo) == btnYes) {

            Session.clear();

            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/gwanhee/groupproject/LogIn.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) sideBar.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } else {
            AlertUtil.infoAlert("Logout Cancelled", "Log out Cancelled");
        }
    }

    public void openprofile() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Profile.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 350, 600);


        Stage stage = (Stage) sideBar.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void openhelp() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/HelpLoggedin.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 350, 600);


        Stage stage = (Stage) sideBar.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void orderHistory() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/OrderHistory.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 350, 600);


        Stage stage = (Stage) sideBar.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}