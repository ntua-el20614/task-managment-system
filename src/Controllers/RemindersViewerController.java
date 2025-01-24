package Controllers;

import src.App;
import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.Optional;
import javafx.scene.image.ImageView;

/**
 * RemindersViewerController.java
 * 
 * Controller for the RemindersViewer.fxml.
 */
public class RemindersViewerController {

    @FXML
    private ListView<String> remindersListView;

    @FXML
    private Button btnAddReminder;

    @FXML
    private Button btnEditReminder;

    @FXML
    private Button btnDeleteReminder;

    @FXML
    private Button btnClose;

    @FXML
    private ImageView addReminderIcon;

    @FXML
    private ImageView editReminderIcon;

    @FXML
    private ImageView deleteReminderIcon;

    private User currentUser;

    @FXML
    public void initialize() {
        // Initialize button icons
        initializeButtonIcons();

        // Initially disable Edit and Delete buttons
        btnEditReminder.setDisable(true);
        btnDeleteReminder.setDisable(true);

        // Add listener to enable buttons when an item is selected
        remindersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean selected = newSelection != null;
            btnEditReminder.setDisable(!selected);
            btnDeleteReminder.setDisable(!selected);
        });
    }

    /**
     * Sets the current user and loads reminders.
     */
    public void setUser(User user) {
        this.currentUser = user;
        loadReminders();
    }

    /**
     * Loads all reminders from the current user's tasks.
     */
    private void loadReminders() {
        ObservableList<String> reminders = FXCollections.observableArrayList();
        for (Task task : currentUser.getTasks()) {
            if (task.getReminders() != null && !task.getReminders().isEmpty()) {
                for (String reminder : task.getReminders()) {
                    reminders.add(task.getTitle() + " | " + reminder);
                }
            }
        }
        remindersListView.setItems(reminders);

        if (reminders.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Reminders", "There are no reminders to display.");
        }
    }

    /**
     * Initializes the icons for the action buttons.
     */
    private void initializeButtonIcons() {
        try {
            addReminderIcon.setImage(new javafx.scene.image.Image(getClass().getResource("/Media/add.png").toExternalForm()));
            editReminderIcon.setImage(new javafx.scene.image.Image(getClass().getResource("/Media/edit.png").toExternalForm()));
            deleteReminderIcon.setImage(new javafx.scene.image.Image(getClass().getResource("/Media/delete.png").toExternalForm()));
        } catch (NullPointerException e) {
            System.err.println("Error loading reminder action icons: " + e.getMessage());
        }
    }

    /**
     * Handles the Add Reminder button action.
     */
    @FXML
    private void handleAddReminder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddEditReminderDialog.fxml"));
            Parent root = loader.load();

            AddEditReminderDialogController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setTask(null, null); // null indicates add mode

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Reminder");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Reload reminders after adding
            reloadUserData();
            loadReminders();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to open Add Reminder dialog.");
            e.printStackTrace();
        }
    }

    /**
     * Handles the Edit Reminder button action.
     */
    @FXML
    private void handleEditReminder() {
        String selected = remindersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a reminder to edit.");
            return;
        }

        // Split the selected string to get task title and reminder
        String[] parts = selected.split(" \\| ");
        if (parts.length != 2) {
            showAlert(AlertType.ERROR, "Error", "Invalid reminder format.");
            return;
        }

        String taskTitle = parts[0];
        String reminder = parts[1];

        // Find the task by title
        Optional<Task> taskOpt = currentUser.getTasks().stream()
                .filter(t -> t.getTitle().equals(taskTitle))
                .findFirst();

        if (!taskOpt.isPresent()) {
            showAlert(AlertType.ERROR, "Error", "Associated task not found.");
            return;
        }

        Task task = taskOpt.get();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddEditReminderDialog.fxml"));
            Parent root = loader.load();

            AddEditReminderDialogController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setTask(task, reminder); // Pass the task and existing reminder for edit mode

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Reminder");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Reload reminders after editing
            reloadUserData();
            loadReminders();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to open Edit Reminder dialog.");
            e.printStackTrace();
        }
    }

    /**
     * Handles the Delete Reminder button action.
     */
    @FXML
    private void handleDeleteReminder() {
        String selected = remindersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a reminder to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Delete Reminder");
        confirm.setHeaderText("Are you sure you want to delete the selected reminder?");
        confirm.setContentText(selected);

        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            // Split the selected string to get task title and reminder
            String[] parts = selected.split(" \\| ");
            if (parts.length != 2) {
                showAlert(AlertType.ERROR, "Error", "Invalid reminder format.");
                return;
            }

            String taskTitle = parts[0];
            String reminder = parts[1];

            // Find the task by title
            Optional<Task> taskOpt = currentUser.getTasks().stream()
                    .filter(t -> t.getTitle().equals(taskTitle))
                    .findFirst();

            if (!taskOpt.isPresent()) {
                showAlert(AlertType.ERROR, "Error", "Associated task not found.");
                return;
            }

            Task task = taskOpt.get();

            // Remove the reminder
            boolean removed = task.getReminders().remove(reminder);
            if (removed) {
                // Persist changes
                JsonUtils.updateUser(currentUser);
                showAlert(AlertType.INFORMATION, "Success", "Reminder deleted successfully.");

                // Reload reminders
                reloadUserData();
                loadReminders();
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to delete the reminder.");
            }
        }
    }

    /**
     * Handles the Close button action.
     */
    @FXML
    private void handleClose() {
        // Close the stage
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }


    /**
     * Reloads the current user data from the data source to ensure the latest updates are reflected.
     */
    private void reloadUserData() {
        // Refresh the user data from the JSON file
        User refreshedUser = JsonUtils.findUser(currentUser.getUsername());
        if (refreshedUser != null) {
            currentUser = refreshedUser; // Update current user with refreshed data
            // Also update the App's currentUser if necessary
            App.setCurrentUser(refreshedUser);
        } else {
            showAlert(AlertType.ERROR, "Error", "Failed to refresh user data.");
        }
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
