package Controllers;

import Models.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.scene.control.Alert;

import src.App;

public class MainControllerWrapper {

    private User currentUser;

    @FXML
    private BorderPane rootPane; // Must match fx:id in FXML

    @FXML
    private ImageView eyeToggle; // ImageView for the eye icon

    @FXML
    private ImageView lockToggle; // ImageView for the lock icon

    @FXML
    private StackPane eyeContainer; // StackPane for the square box

    @FXML
    private StackPane lockContainer; // StackPane for the lock box

    @FXML
    private HBox buttonContainer; // Container for the buttons (aligned right)

    @FXML
    private Text welcomeText; // Text for the welcome message

    private boolean isDetailedView = true;

    // Paths to the eye and lock images
    private final Image openEyeImage = new Image(App.class.getResource("/Media/eye/open_eye.png").toExternalForm());
    private final Image closedEyeImage = new Image(App.class.getResource("/Media/eye/closed_eye.png").toExternalForm());
    private final Image lockImage = new Image(App.class.getResource("/Media/lock.png").toExternalForm());

    @FXML
    public void initialize() {
        currentUser = App.getCurrentUser(); // Fetch the current user
        loadDetailedView(); // Set default view
        updateEyeIcon();    // Set the initial image
        setupLockIcon();    // Initialize the lock button
        setupWelcomeMessage(); // Set the welcome message
        adjustLayout(); // Adjust layout programmatically if needed
        showReminders(); // Show reminders if any
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

    private void setupLockIcon() {
        // Set the initial lock image
        lockToggle.setImage(lockImage);
    }

    private void setupWelcomeMessage() {
        String currentUser = App.getCurrentUsername(); // Replace with actual method to fetch the username
        welcomeText.setText("WELCOME TO TASKFLOW " + (currentUser != null ? currentUser.toUpperCase() : "GUEST") + "!");
    }

    private void adjustLayout() {
        HBox.setMargin(eyeContainer, new Insets(0, 5, 0, 0));
        HBox.setMargin(lockContainer, new Insets(0, 0, 0, 5));
    }

    public void handleLogout() {
        System.out.println("Logout initiated. Scene: " + rootPane.getScene());
        if (rootPane.getScene() != null) {
            System.out.println("Window: " + rootPane.getScene().getWindow());
        }
        closeWindow();
    }

    private void showReminders() {
        LocalDate today = LocalDate.now();

        currentUser.getTasks().stream()
            .filter(task -> task.getReminders() != null)
            .forEach(task -> {
                task.getReminders().forEach(reminder -> {
                    try {
                        // Extract the date part from the reminder string
                        String datePart = reminder.replaceAll(".*\\((\\d{4}-\\d{2}-\\d{2})\\).*", "$1");
                        LocalDate reminderDate = LocalDate.parse(datePart);

                        if (reminderDate.isEqual(today)) {
                            openReminderWindow(task.getTitle(), task.getDescription(), task.getDeadline(), reminder);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse reminder date: " + reminder + " - " + e.getMessage());
                    }
                });
            });
    }


    private void openReminderWindow(String title, String description, String deadline, String reminderDate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReminderAlertView.fxml"));
            Parent root = loader.load();

            ReminderAlertController controller = loader.getController();
            controller.setReminderDetails(title, description, deadline, reminderDate);

            Stage stage = new Stage();
            stage.setTitle("Reminder");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Log the exception stack trace

            // Directly create and show an alert here
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null); // No header
            alert.setContentText("Failed to load Reminder window. " + e.getMessage());
            alert.showAndWait();
        }
    }


    public void closeWindow() {
        if (rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            // Redirect to login page
            App.showLoginView();

            stage.hide();
        } else {
            System.err.println("Cannot close window: Scene or Window is null.");
        }
    }
}
