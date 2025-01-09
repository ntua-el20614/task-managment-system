package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

// Import your App class (replace with the correct package)
import src.App;


public class MainControllerWrapper {

    @FXML
    private BorderPane rootPane; // Must match fx:id in FXML

    private boolean isDetailedView = true;

    @FXML
    public void initialize() {
        loadDetailedView();
    }

    @FXML
    private void handleToggleView() {
        if (isDetailedView) {
            loadListView();
        } else {
            loadDetailedView();
        }
        isDetailedView = !isDetailedView;
    }

    private void loadDetailedView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/MainViewDetail.fxml"));
            Parent detailedView = loader.load();
            rootPane.setCenter(detailedView); // Ensure rootPane is initialized
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadListView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/MainViewList.fxml"));
            Parent listView = loader.load();
            rootPane.setCenter(listView); // Ensure rootPane is initialized
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
