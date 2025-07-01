package aurora;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class DashboardController {
    @FXML private Label welcomeLabel, dateLabel, kpLabel, alertLabel;
    @FXML private ComboBox<String> regionBox;
    private boolean alertShown = false;

    public void initialize() {
        regionBox.getItems().addAll("Tromsø", "Fairbanks", "Reykjavik", "Abisko", "Yukon", "Luosto", "Yellowknife",
                "Murmansk", "Kiruna", "Rovaniemi");
        dateLabel.setText("Date: " + LocalDate.now());
    }

    public void setUser(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    @FXML
    private void showForecast() {
        String region = regionBox.getValue();
        if (region == null) return;

        int kp = new Random().nextInt(9);
        kpLabel.setText("KP Index: " + kp);
        alertLabel.setText(kp >= 5 ? "Aurora likely visible!" : "Aurora not visible.");

        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5), alertLabel);
        tt.setFromY(-20);
        tt.setToY(0);
        tt.play();

        if (kp >= 5 && !alertShown) {
            showAuroraPopup(kp);
            alertShown = true;
        }

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO forecast_history (username, region, kp_index) VALUES (?, ?, ?)");
            stmt.setString(1, LoginController.currentUser);
            stmt.setString(2, region);
            stmt.setInt(3, kp);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAuroraPopup(int kp) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Aurora Alert");
        popupStage.setResizable(false);
        popupStage.setAlwaysOnTop(true);

        Label title = new Label("Aurora Alert!");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        Label message = new Label("KP Index: " + kp + "\nThe auroras might be dancing tonight! 🌌");
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
        
        ImageView auroraImage;
        String imagePath;

        if (kp == 5) {
            imagePath = "/images/aurora.gif";
        } else if (kp == 6) {
            imagePath = "/images/aurora1.jpg";
        } else if (kp == 7) {
            imagePath = "/images/aurora2.jpg";
        } else if (kp == 8) {
            imagePath = "/images/aurora3.jpg";
        } else if (kp == 9) {
            imagePath = "/images/aurora4.jpg";
        } else {
            imagePath = "/images/aurora5.jpg";
        }

        try {
            auroraImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            auroraImage = new ImageView();
            System.err.println("Could not load image: " + imagePath);
        }

        auroraImage.setFitWidth(250);
        auroraImage.setPreserveRatio(true);

        VBox content = new VBox(10, title, auroraImage, message);
        content.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20px; -fx-alignment: center; -fx-border-radius: 12px;");
        content.setOpacity(0);

        Scene popupScene = new Scene(content);
        popupStage.setScene(popupScene);

        MediaPlayer mediaPlayer = null;
        try {
            Media media = new Media(getClass().getResource("/audio/aurora.mp3").toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load audio.");
        }

        MediaPlayer finalMediaPlayer = mediaPlayer;
        popupStage.setOnCloseRequest(event -> {
            if (finalMediaPlayer != null) finalMediaPlayer.stop();
        });

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            PauseTransition wait = new PauseTransition(Duration.seconds(10));
            wait.setOnFinished(ev -> {
                if (finalMediaPlayer != null) finalMediaPlayer.stop();  // 💥 Stop music
                popupStage.close();
            });
            wait.play();
        });

        popupStage.show();
        fadeIn.play();
    }

    @FXML
    private void logout() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(root);

        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        Stage stage = (Stage) regionBox.getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    private void viewHistory() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/History.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        Stage stage = (Stage) regionBox.getScene().getWindow();
        stage.setScene(scene);
    }
}
