package Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

import src.App;

public class MainControllerWrapper {

    @FXML
    private BorderPane rootPane; // Must match fx:id in FXML

    @FXML
    private ImageView eyeToggle; // ImageView for the eye icon

    @FXML
    private StackPane eyeContainer; // StackPane for the square box

    private boolean isDetailedView = true;

    // Paths to the eye images in the Media/eye folder
    private final Image openEyeImage = new Image(App.class.getResource("/Media/eye/open_eye.png").toExternalForm());
    private final Image closedEyeImage = new Image(App.class.getResource("/Media/eye/closed_eye.png").toExternalForm());

    @FXML
    public void initialize() {
        loadDetailedView(); // Set default view
        updateEyeIcon();    // Set the initial image
        adjustEyeContainer(); // Adjust the layout programmatically if needed
    }

    @FXML
    private void handleToggleView() {
        if (isDetailedView) {
            loadListView();
            animateEyeChange(openEyeImage);
        } else {
            loadDetailedView();
            animateEyeChange(closedEyeImage);
        }
        isDetailedView = !isDetailedView;
    }

    private void loadDetailedView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/MainViewDetail.fxml"));
            Parent detailedView = loader.load();
            rootPane.setCenter(detailedView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadListView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/MainViewList.fxml"));
            Parent listView = loader.load();
            rootPane.setCenter(listView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateEyeIcon() {
        // Set the initial eye image
        eyeToggle.setImage(isDetailedView ? closedEyeImage : openEyeImage);
    }

    private void animateEyeChange(Image newImage) {
        // Fade out current image
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), eyeToggle);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            // Change the image once fade-out completes
            eyeToggle.setImage(newImage);

            // Fade in the new image
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), eyeToggle);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void adjustEyeContainer() {
        // Optionally add margins or paddings if needed
        StackPane.setMargin(eyeContainer, new Insets(10, 10, 0, 0));
    }
}
