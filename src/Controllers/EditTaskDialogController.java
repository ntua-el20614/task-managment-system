package Controllers;

import Models.Task;
import Models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * EditTaskDialogController.java
 * 
 * Controller for the EditTaskDialog.fxml.
 */
public class EditTaskDialogController {

    @FXML
    private TextField txtTitle;

    @FXML
    private TextArea txtDescription;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private ComboBox<String> cmbPriority;

    @FXML
    private DatePicker dpDeadline;

    @FXML
    private ComboBox<String> cmbStatus;

    @FXML
    private Button btnSave;

    private User currentUser;
    private Task taskToEdit;
    private Task updatedTask;

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Disable the Save button initially
        btnSave.setDisable(true);

        // Add listeners to enable the Save button when title is not empty
        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            btnSave.setDisable(newValue.trim().isEmpty());
        });
    }

    /**
     * Sets the current user and task to edit, and populates the fields.
     * 
     * @param user The current authenticated user.
     * @param task The task to edit.
     */
    public void setTask(User user, Task task) {
        this.currentUser = user;
        this.taskToEdit = task;

        txtTitle.setText(task.getTitle());
        txtDescription.setText(task.getDescription());
        cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
        cmbCategory.setValue(task.getCategory());
        cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));
        cmbPriority.setValue(task.getPriority());
        dpDeadline.setValue(LocalDate.parse(task.getDeadline(), DateTimeFormatter.ISO_DATE));
        cmbStatus.setItems(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed", "Delayed"));
        cmbStatus.setValue(task.getStatus());
    }

    /**
     * Handles the action when the "Save" button is pressed.
     */
    @FXML
    private void handleSave() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String category = cmbCategory.getValue();
        String priority = cmbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();
        String status = cmbStatus.getValue();

        if (title.isEmpty()) {
            // Should not happen as Save button is disabled
            return;
        }

        updatedTask = new Task(title, description, category, priority, deadline.format(DateTimeFormatter.ISO_DATE), status);
        updatedTask.setReminders(taskToEdit.getReminders()); // Preserve reminders

        // Close the dialog
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the action when the "Cancel" button is pressed.
     */
    @FXML
    private void handleCancel() {
        updatedTask = null;
        // Close the dialog
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Returns the updated task.
     * 
     * @return The updated Task object.
     */
    public Optional<Task> getUpdatedTask() {
        return Optional.ofNullable(updatedTask);
    }
}
