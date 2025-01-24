package Controllers;

import src.App;
import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

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

    // New fields for reminders
    @FXML
    private ComboBox<String> cmbReminderType;

    @FXML
    private DatePicker dpReminderDate;

    @FXML
    private Button btnAddReminder;
    
    @FXML
    private Button btnDeleteReminder;

    @FXML
    private ListView<String> lstReminders;

    private ObservableList<String> reminderList = FXCollections.observableArrayList();

    private User currentUser;
    private Task currentTask;
    private Consumer<Void> onTaskUpdatedCallback; // Callback for notifying task update

    @FXML
    private void initialize() {
        // Disable the Save button initially until the title is entered
        btnSave.setDisable(true);

        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            btnSave.setDisable(newValue.trim().isEmpty());
        });

        // Initialize reminder type selection
        cmbReminderType.setItems(FXCollections.observableArrayList(
            "One day before deadline",
            "One week before deadline",
            "One month before deadline",
            "Specific date"
        ));
        cmbReminderType.getSelectionModel().selectFirst();
        dpReminderDate.setDisable(true);

        // Enable date picker only for "Specific date" selection
        cmbReminderType.valueProperty().addListener((obs, oldVal, newVal) -> {
            dpReminderDate.setDisable(!"Specific date".equals(newVal));
        });

        // Bind Delete Reminder button disable property to ListView selection
        btnDeleteReminder.disableProperty().bind(
            lstReminders.getSelectionModel().selectedItemProperty().isNull()
        );

        lstReminders.setItems(reminderList);

        // Disable reminder-related controls when the status is "Completed"
        cmbStatus.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("Completed".equals(newValue)) {
                // Clear all reminders
                reminderList.clear();

                // Unbind before setting disable to avoid conflicts
                btnAddReminder.disableProperty().unbind();
                btnDeleteReminder.disableProperty().unbind();
                cmbReminderType.disableProperty().unbind();
                dpReminderDate.disableProperty().unbind();

                // Disable reminder controls
                cmbReminderType.setDisable(true);
                dpReminderDate.setDisable(true);
                btnAddReminder.setDisable(true);
                btnDeleteReminder.setDisable(true);
            } else {
                // Enable reminder controls for other statuses
                cmbReminderType.setDisable(false);
                dpReminderDate.setDisable(!"Specific date".equals(cmbReminderType.getValue()));

                // Rebind Delete Reminder button's disable property
                btnDeleteReminder.disableProperty().bind(
                    lstReminders.getSelectionModel().selectedItemProperty().isNull()
                );
                btnAddReminder.setDisable(false);
            }
        });
    }



    /**
     * Called externally to set the current user.
     * This triggers a refresh to load categories, priorities, and statuses.
     */
    public void setUser(User user) {
        // Make sure we load the most up-to-date user from your App
        App.refreshCurrentUser();
        this.currentUser = App.getCurrentUser();
        
        // Or simply:
        // this.currentUser = user;

        refreshCategoriesPrioritiesAndStatus();
    }

    /**
     * Populate the category, priority, and status ComboBoxes
     * from the current user's data (or some hardcoded default).
     */
    private void refreshCategoriesPrioritiesAndStatus() {
        if (currentUser != null) {
            // Category & Priority from User model
            cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
            cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));

            // Status items — if your User model also stores statuses, use that:
            // cmbStatus.setItems(FXCollections.observableArrayList(currentUser.getStatuses()));
            // Otherwise, hardcode or fetch from somewhere else:
            cmbStatus.setItems(FXCollections.observableArrayList("Open", "In Progress", "Completed", "Delayed","Postponed"));

            // Optionally select first by default if not empty
            if (!cmbCategory.getItems().isEmpty()) {
                cmbCategory.getSelectionModel().selectFirst();
            }
            if (!cmbPriority.getItems().isEmpty()) {
                cmbPriority.getSelectionModel().selectFirst();
            }
            if (!cmbStatus.getItems().isEmpty()) {
                cmbStatus.getSelectionModel().selectFirst();
            }
        }
    }

    /**
     * Allows external injection of a callback so that
     * when the task is updated, the main UI can refresh.
     */
    public void setOnTaskUpdatedCallback(Consumer<Void> onTaskUpdatedCallback) {
        this.onTaskUpdatedCallback = onTaskUpdatedCallback;
    }

    /**
     * If you need to refresh data from the outside again.
     */
    public void refreshData() {
        if (currentUser != null) {
            refreshCategoriesPrioritiesAndStatus();
        }
    }

    /**
     * Overloaded setTask method for convenience if you want
     * to pass User and Task together.
     */
    public void setTask(User user, Task task) {
        this.currentUser = user;
        refreshCategoriesPrioritiesAndStatus();
        setTask(task);
    }

    /**
     * This is the primary setTask method that populates the form
     * with the existing task data.
     */
    public void setTask(Task task) {
        this.currentTask = task;

        if (task == null) {
            return;
        }

        // Populate fields with existing task details
        txtTitle.setText(task.getTitle());
        txtDescription.setText(task.getDescription());
        cmbCategory.setValue(task.getCategory());
        cmbPriority.setValue(task.getPriority());

        // Parse the deadline LocalDate
        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            dpDeadline.setValue(LocalDate.parse(task.getDeadline()));
        } else {
            dpDeadline.setValue(null);
        }

        // Status
        cmbStatus.setValue(task.getStatus());

        // Load existing reminders into the list
        reminderList.clear();
        if (task.getReminders() != null) {
            reminderList.addAll(task.getReminders());
        }
    }

    /**
     * Retrieve the updated Task after Save if needed.
     */
    public Optional<Task> getUpdatedTask() {
        return Optional.ofNullable(currentTask);
    }

    /**
     * Called when "Delete Reminder" button is pressed.
     */
    @FXML
    private void handleDeleteReminder() {
        String selected = lstReminders.getSelectionModel().getSelectedItem();
        if (selected != null) {
            reminderList.remove(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Reminder Selected");
            alert.setContentText("Please select a reminder to delete.");
            alert.showAndWait();
        }
    }

    /**
     * Called when "Add Reminder" button is pressed.
     */
    @FXML
    private void handleAddReminder() {
        String type = cmbReminderType.getValue();
        LocalDate deadline = dpDeadline.getValue();

        // Ensure deadline is selected
        if (deadline == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Deadline Required");
            alert.setContentText("Please select a deadline before adding reminders.");
            alert.showAndWait();
            return;
        }

        LocalDate reminderDate = null;

        // Determine reminder date based on type
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
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Date Required");
                    alert.setContentText("Please select a date for the reminder.");
                    alert.showAndWait();
                    return;
                }
                break;
        }

        // Ensure reminder date is valid
        if (reminderDate.isBefore(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Reminder Date");
            alert.setContentText("The reminder date has already passed.");
            alert.showAndWait();
            return;
        }

        if (reminderDate.isAfter(deadline)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Reminder Date");
            alert.setContentText("The reminder date cannot be after the deadline.");
            alert.showAndWait();
            return;
        }

        // Check for duplicates
        String reminderDateStr = reminderDate.format(DateTimeFormatter.ISO_DATE);
        boolean isDuplicate = reminderList.stream()
                .anyMatch(reminder -> reminder.contains(reminderDateStr));

        if (isDuplicate) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Reminder");
            alert.setHeaderText("Reminder Already Exists");
            alert.setContentText("A reminder for this date already exists.");
            alert.showAndWait();
            return;
        }

        // Add the reminder to the list
        // This line differs from your 'AddTaskDialog' — 
        // you might want to keep the same format (including the type) if needed:
        // e.g. reminderList.add(type + " (" + reminderDateStr + ")");
        reminderList.add(reminderDateStr);
    }

    /**
     * Called when "Save" button is pressed.
     */
    @FXML
    private void handleSave() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String category = cmbCategory.getValue();
        String priority = cmbPriority.getValue();
        String status = cmbStatus.getValue();
        LocalDate deadline = dpDeadline.getValue();

        // Basic validation
        if (title.isEmpty() || deadline == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Title and Deadline are required!");
            alert.showAndWait();
            return;
        }

        // Update task details
        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setCategory(category);
        currentTask.setPriority(priority);
        currentTask.setDeadline(deadline.format(DateTimeFormatter.ISO_DATE));
        currentTask.setStatus(status);

        // Update reminders
        if (currentTask.getReminders() != null) {
            currentTask.getReminders().clear();
        }
        for (String reminderInfo : reminderList) {
            currentTask.addReminder(reminderInfo);
        }

        // Persist changes
        JsonUtils.updateUser(currentUser);

        // Callback to refresh main view or tasks list
        if (onTaskUpdatedCallback != null) {
            onTaskUpdatedCallback.accept(null);
        }

        // Close the dialog
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Called when "Cancel" button is pressed.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
