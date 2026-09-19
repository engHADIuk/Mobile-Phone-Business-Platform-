package org.gwanhee.groupproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class LogInController {

    public static String id1 = "";

    @FXML
    private TextField logInId;

    @FXML
    private PasswordField logInPassword;

    @FXML
    private Label welcomeText;

    @FXML
    private void logInDone() throws IOException {
        String id = logInId.getText().trim();
        String password = logInPassword.getText();

        if (id.isEmpty() || password.isEmpty()) {
            AlertUtil.warningAlert("Missing Infomation", "Please enter ID and password");
            return;
        }

        boolean success = CustomerDAO.login(id, password);

        if (success) {

            boolean success1 = CustomerDAO.isAccountActive(id);

            if(success1){
                Session.setCurrentLoginId(id);

                FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("/org/gwanhee/groupproject/Main.fxml")
                );

                Scene scene = new Scene(fxmlLoader.load(), 350, 600);

                Stage stage = (Stage) logInId.getScene().getWindow();
                stage.setTitle("Main");
                stage.setScene(scene);
                stage.show();

            } else if (!success1) {
                AlertUtil.warningAlert("Account is not active", "This account is not active");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Account is not active");
                alert.setContentText("If you press Yes, you will be move to activation page");
                ButtonType btnYes = new ButtonType("Yes");
                ButtonType btnNo = new ButtonType("No");
                alert.getButtonTypes().setAll(btnYes, btnNo);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.orElse(btnNo) == btnYes) {
                    FXMLLoader fxmlLoader = new FXMLLoader(
                            getClass().getResource("/org/gwanhee/groupproject/Activate.fxml")
                    );

                    Scene scene = new Scene(fxmlLoader.load());

                    Stage stage = (Stage) logInId.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                }
            }

        } else {
            AlertUtil.errorAlert("Login Failed", "Invalid ID or Password");
        }
    }

    @FXML
    private void regist() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Register.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) logInId.getScene().getWindow();
        stage.setTitle("Regist");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void loginhelp() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/gwanhee/groupproject/Help.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 350, 600);

        Stage stage = (Stage) logInId.getScene().getWindow();
        stage.setTitle("Regist");
        stage.setScene(scene);
        stage.show();
    }
}
