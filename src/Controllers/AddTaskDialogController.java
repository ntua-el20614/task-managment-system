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
 * AddTaskDialogController.java
 * 
 * Controller for the AddTaskDialog.fxml.
 */
public class AddTaskDialogController {

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
    private TextField cmbStatus;

    @FXML
    private Button btnAdd;

    // New fields for reminders
    @FXML
    private ComboBox<String> cmbReminderType;

    @FXML
    private DatePicker dpReminderDate;

    @FXML
    private Button btnAddReminder;

    @FXML
    private ListView<String> lstReminders;

    private ObservableList<String> reminderList = FXCollections.observableArrayList();

    private User currentUser;
    private Task newTask;
    private Consumer<Void> onTaskAddedCallback; // Callback for notifying task addition

    @FXML
    private void initialize() {
        // Disable the Add button initially
        btnAdd.setDisable(true);

        // Enable the Add button only when the title field is not empty
        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            btnAdd.setDisable(newValue.trim().isEmpty());
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

        lstReminders.setItems(reminderList);
    }

    public void setUser(User user) {
        // Refresh the current user data
        App.refreshCurrentUser();
        this.currentUser = App.getCurrentUser();
        refreshCategoriesAndPriorities();
    }

    private void refreshCategoriesAndPriorities() {
        if (currentUser != null) {
            cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
            cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));
            cmbCategory.getSelectionModel().selectFirst();
            cmbPriority.getSelectionModel().selectFirst();
        }
    }

    public void setOnTaskAddedCallback(Consumer<Void> onTaskAddedCallback) {
        this.onTaskAddedCallback = onTaskAddedCallback;
    }

    public void refreshData() {
        if (currentUser != null) {
            cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
            cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));
            cmbCategory.getSelectionModel().selectFirst();
            cmbPriority.getSelectionModel().selectFirst();
        }
    }


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
        reminderList.add(reminderDateStr);
    }



    @FXML
    private void handleDeleteReminder() {
        String selected = lstReminders.getSelectionModel().getSelectedItem();
        if(selected != null) {
            reminderList.remove(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Reminder Selected");
            alert.setContentText("Please select a reminder to delete.");
            alert.showAndWait();
        }
    }


    @FXML
    private void handleAdd() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String category = cmbCategory.getValue();
        String priority = cmbPriority.getValue();
        String status = "Open";
        LocalDate deadline = dpDeadline.getValue();

        if (title.isEmpty() || deadline == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Title and Deadline are required!");
            alert.showAndWait();
            return;
        }

        newTask = new Task(title, description, category, priority, deadline.format(DateTimeFormatter.ISO_DATE), status);

        // Associate reminders with the new task
        for(String reminderInfo : reminderList) {
            newTask.addReminder(reminderInfo);
        }

        // Add the new task to the current user's task list
        currentUser.addTask(newTask);

        // Persist the updated user data
        JsonUtils.updateUser(currentUser);

        // Trigger the reload callback
        if (onTaskAddedCallback != null) {
            onTaskAddedCallback.accept(null); // Notify the MainViewController
        }

        // Close the dialog
        Stage stage = (Stage) btnAdd.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        newTask = null;
        Stage stage = (Stage) btnAdd.getScene().getWindow();
        stage.close();
    }

    public Optional<Task> getNewTask() {
        return Optional.ofNullable(newTask);
    }
}
