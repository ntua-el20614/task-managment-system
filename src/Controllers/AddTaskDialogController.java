package Controllers;

import src.App;
import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.collections.FXCollections;
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
    private ComboBox<String> cmbStatus;

    @FXML
    private Button btnAdd;

    private User currentUser;
    private Task newTask;
    private Consumer<Void> onTaskAddedCallback; // Callback for notifying task addition

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Populate the Status ComboBox
        cmbStatus.setItems(FXCollections.observableArrayList(
            "Open", 
            "In Progress", 
            "Postponed", 
            "Delayed", 
            "Completed"
        ));
    
        // Select the default value for the Status ComboBox
        cmbStatus.getSelectionModel().selectFirst();
    
        // Disable the Add button initially
        btnAdd.setDisable(true);
    
        // Enable the Add button only when the title field is not empty
        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            btnAdd.setDisable(newValue.trim().isEmpty());
        });
    }


    /**
     * Sets the current user and refreshes the ComboBoxes.
     * 
     * @param user The current authenticated user.
     */
    public void setUser(User user) {
        // Refresh the current user data
        App.refreshCurrentUser();
        this.currentUser = App.getCurrentUser();
        refreshCategoriesAndPriorities();
    }

    
    /**
     * Refreshes the categories and priorities in the ComboBoxes.
     */
    private void refreshCategoriesAndPriorities() {
        if (currentUser != null) {
            cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
            cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));
            cmbCategory.getSelectionModel().selectFirst();
            cmbPriority.getSelectionModel().selectFirst();
        }
    }


    /**
     * Sets the callback to be executed after a task is added.
     * 
     * @param onTaskAddedCallback The callback function.
     */
    public void setOnTaskAddedCallback(Consumer<Void> onTaskAddedCallback) {
        this.onTaskAddedCallback = onTaskAddedCallback;
    }

    /**
     * Refreshes the categories and priorities in the ComboBoxes.
     */
    public void refreshData() {
        if (currentUser != null) {
            cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
            cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));
            cmbCategory.getSelectionModel().selectFirst();
            cmbPriority.getSelectionModel().selectFirst();
        }
    }


    /**
     * Handles the action when the "Add" button is pressed.
     */
    @FXML
    private void handleAdd() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String category = cmbCategory.getValue();
        String priority = cmbPriority.getValue();
        String status = Optional.ofNullable(cmbStatus.getValue()).orElse("Open");
        LocalDate deadline = dpDeadline.getValue();

        if (title.isEmpty() || deadline == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Title and Deadline are required!");
            alert.showAndWait();
            return;
        }

        // Default status is "Open" if not provided

        newTask = new Task(title, description, category, priority, deadline.format(DateTimeFormatter.ISO_DATE), status);

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

    /**
     * Handles the action when the "Cancel" button is pressed.
     */
    @FXML
    private void handleCancel() {
        newTask = null;
        // Close the dialog
        Stage stage = (Stage) btnAdd.getScene().getWindow();
        stage.close();
    }

    /**
     * Returns the newly created task.
     * 
     * @return The new Task object.
     */
    public Optional<Task> getNewTask() {
        return Optional.ofNullable(newTask);
    }
}
