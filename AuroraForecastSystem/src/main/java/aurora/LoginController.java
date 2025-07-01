package aurora;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.sql.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    public static String currentUser;

    @FXML
    private void login(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter all fields.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentUser = username;

                // ✅ Load FXML with controller access
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                Parent root = loader.load();

                // ✅ Pass username to dashboard
                DashboardController dashboardController = loader.getController();
                dashboardController.setUser(currentUser);

                // ✅ Transition
                Scene scene = new Scene(root);
                FadeTransition ft = new FadeTransition(Duration.millis(800), root);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
            } else {
                errorLabel.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database error.");
        }
    }

    @FXML
    private void goToRegister() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Register.fxml"));
        Scene scene = new Scene(root);

        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
        System.out.println("Clicked the link");
    }
}
