package aurora;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.sql.*;

public class HistoryController {
    @FXML private TableView<ForecastHistory> historyTable;
    @FXML private TableColumn<ForecastHistory, String> regionCol;
    @FXML private TableColumn<ForecastHistory, Integer> kpCol;
    @FXML private TableColumn<ForecastHistory, String> timeCol;

    private ObservableList<ForecastHistory> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        regionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRegion()));
        kpCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getKp()).asObject());
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTime()));

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT region, kp_index, timestamp FROM forecast_history WHERE username = ?");
            stmt.setString(1, LoginController.currentUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                historyList.add(new ForecastHistory(
                        rs.getString("region"),
                        rs.getInt("kp_index"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        historyTable.setItems(historyList);
    }

    @FXML
    private void goBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
        Scene scene = new Scene(root);

        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        Stage stage = (Stage) historyTable.getScene().getWindow();
        stage.setScene(scene);
    }
}
