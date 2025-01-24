package Controllers;

import Models.User;
import Models.Task;
import Utils.JsonUtils;
import java.util.stream.Collectors;
import java.util.List; 
import src.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;

/**
 * AddEditReminderDialogController.java
 * 
 * Controller for the AddEditReminderDialog.fxml.
 */
public class AddEditReminderDialogController {

    @FXML
    private ComboBox<String> cmbReminderType;

    @FXML
    private ComboBox<String> cmbTaskSelector;

    @FXML
    private DatePicker dpReminderDate;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private User currentUser;
    private Task currentTask;
    private ObservableList<Task> taskList;
    private String existingReminder; // Holds the reminder being edited, null if adding

    private boolean isEditMode = false; // Flag to determine if editing

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Populate the ComboBox with reminder types
        cmbReminderType.setItems(FXCollections.observableArrayList(
            "One day before deadline",
            "One week before deadline",
            "One month before deadline",
            "Specific date"
        ));

        cmbReminderType.setStyle("-fx-text-fill: white;");
        cmbTaskSelector.setStyle("-fx-text-fill: white;");

        // Disable the DatePicker initially
        dpReminderDate.setDisable(true);

        // Enable DatePicker only when "Specific date" is selected
        cmbReminderType.valueProperty().addListener((obs, oldVal, newVal) -> {
            dpReminderDate.setDisable(!"Specific date".equals(newVal));
            if (!"Specific date".equals(newVal)) {
                dpReminderDate.setValue(null); // Clear date if not specific
            }
        });

        // Disable save button until valid input is provided
        btnSave.setDisable(true);
        cmbTaskSelector.valueProperty().addListener((obs, oldVal, newVal) -> validateInput());
        cmbReminderType.valueProperty().addListener((obs, oldVal, newVal) -> validateInput());
        dpReminderDate.valueProperty().addListener((obs, oldVal, newVal) -> validateInput());
    }

    
    public void setUser(User user) {
        this.currentUser = user;
        loadTasks(); // Reload the tasks for the current user
    }


    private void loadTasks() {
        if (!isEditMode) { // Only load tasks when not in edit mode
            currentUser = App.getCurrentUser();
            if (currentUser != null) {
                taskList = FXCollections.observableArrayList(currentUser.getTasks());
                List<String> taskTitles = taskList.stream().map(Task::getTitle).collect(Collectors.toList());
                cmbTaskSelector.setItems(FXCollections.observableArrayList(taskTitles));
                
                cmbTaskSelector.setStyle("-fx-text-fill: white;");
            }
        }
    }


    private void validateInput() {
        String selectedTask = cmbTaskSelector.getValue();
        String reminderType = cmbReminderType.getValue();
        LocalDate specificDate = dpReminderDate.getValue();

        // Enable save button only if a task is selected and valid reminder type is provided
        boolean isValid = selectedTask != null && reminderType != null && (!"Specific date".equals(reminderType) || specificDate != null);
        btnSave.setDisable(!isValid);
    }

    /**
     * Sets the current task and initializes the dialog for editing if a reminder is provided.
     *
     * @param task     The task to which the reminder belongs.
     * @param reminder The existing reminder to edit, null if adding a new reminder.
     */
    public void setTask(Task task, String reminder) {
        this.currentTask = task;
        this.isEditMode = (task != null && reminder != null);
        this.existingReminder = reminder;

        if (isEditMode) {
            // In edit mode, pre-select the task name and disable the task selector
            cmbTaskSelector.setValue(task.getTitle());
            cmbTaskSelector.setDisable(true); // Make the task selection non-editable
            populateFields(reminder); // Populate the reminder fields
        } else {
            // In add mode, enable the task selector and clear any selections
            cmbTaskSelector.setDisable(false);
            cmbTaskSelector.getSelectionModel().clearSelection();
        }
    }


    /**
     * Populates the fields when editing an existing reminder.
     *
     * @param reminder The reminder to edit.
     */
    private void populateFields(String reminder) {
        // Assuming reminders are stored as dates in ISO format
        try {
            LocalDate reminderDate = LocalDate.parse(reminder, DateTimeFormatter.ISO_DATE);
            // Determine the type based on the reminderDate and task deadline
            LocalDate deadline = LocalDate.parse(currentTask.getDeadline(), DateTimeFormatter.ISO_DATE);
            long daysBetween = deadline.toEpochDay() - reminderDate.toEpochDay();

            if (daysBetween == 1) {
                cmbReminderType.setValue("One day before deadline");
            } else if (daysBetween == 7) {
                cmbReminderType.setValue("One week before deadline");
            } else if (daysBetween == 30) {
                cmbReminderType.setValue("One month before deadline");
            } else {
                cmbReminderType.setValue("Specific date");
                dpReminderDate.setValue(reminderDate);
            }
            
            cmbReminderType.setStyle("-fx-text-fill: white;");
        } catch (Exception e) {
            // If parsing fails, default to Specific date
            cmbReminderType.setValue("Specific date");
            dpReminderDate.setValue(null);
            
            cmbReminderType.setStyle("-fx-text-fill: white;");
        }
    }

    /**
     * Handles the Save button action to add or update the reminder.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        String selectedTaskTitle = cmbTaskSelector.getValue();
        if (selectedTaskTitle == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a task.");
            return;
        }

        String type = cmbReminderType.getValue();
        if (type == null || type.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a reminder type.");
            return;
        }

        LocalDate reminderDate = null;
        Task selectedTask = taskList.stream()
            .filter(task -> task.getTitle().equals(selectedTaskTitle))
            .findFirst()
            .orElse(null);

        if (selectedTask == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Selected task not found.");
            return;
        }

        LocalDate deadline = LocalDate.parse(selectedTask.getDeadline(), DateTimeFormatter.ISO_DATE);

        // Determine the reminder date based on type
        switch (type) {
            case "One day before deadline":
                reminderDate = deadline.minusDays(1);
                break;
            case "One week before deadline":
                reminderDate = deadline.minusWeeks(1);
                break;
            case "One month before deadline":
                reminderDate = deadline.minusMonths(1);
                break;
            case "Specific date":
                reminderDate = dpReminderDate.getValue();
                if (reminderDate == null) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a specific date for the reminder.");
                    return;
                }
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid reminder type selected.");
                return;
        }

        // Validation: Reminder date should be before the deadline
        if (reminderDate.isAfter(deadline)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Reminder date cannot be after the deadline.");
            return;
        }

        // Validation: Reminder date should not be in the past
        if (reminderDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Reminder date cannot be in the past.");
            return;
        }

        // Format the reminder date
        String reminderStr = reminderDate.format(DateTimeFormatter.ISO_DATE);

        if (isEditMode) {
            // Editing an existing reminder
            Optional<String> reminderToEdit = selectedTask.getReminders().stream()
                    .filter(r -> r.equals(existingReminder))
                    .findFirst();

            if (reminderToEdit.isPresent()) {
                selectedTask.getReminders().remove(reminderToEdit.get());
                selectedTask.getReminders().add(reminderStr);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Original reminder not found.");
                return;
            }
        } else {
            // Adding a new reminder
            // Check for duplicates
            boolean isDuplicate = selectedTask.getReminders().stream()
                    .anyMatch(r -> r.equals(reminderStr));

            if (isDuplicate) {
                showAlert(Alert.AlertType.WARNING, "Duplicate Reminder", "A reminder for this date already exists.");
                return;
            }

            selectedTask.getReminders().add(reminderStr);
        }

        // Persist changes
        JsonUtils.updateUser(currentUser);

        // Close the dialog
        closeWindow();
    }

    /**
     * Handles the Cancel button action to close the dialog without saving.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    /**
     * Closes the current window.
     */
    private void closeWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an alert dialog.
     *
     * @param type    The type of alert.
     * @param title   The title of the alert.
     * @param message The content message of the alert.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
