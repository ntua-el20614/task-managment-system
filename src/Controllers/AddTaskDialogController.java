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

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Disable the Add button initially
        btnAdd.setDisable(true);

        // Add listeners to enable the Add button when title is not empty
        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            btnAdd.setDisable(newValue.trim().isEmpty());
        });
    }

    /**
     * Sets the current user and populates the ComboBoxes.
     * 
     * @param user The current authenticated user.
     */
    public void setUser(User user) {
        this.currentUser = user;
        cmbCategory.setItems(FXCollections.observableArrayList(currentUser.getCategories()));
        cmbPriority.setItems(FXCollections.observableArrayList(currentUser.getPriorities()));
        cmbCategory.getSelectionModel().selectFirst();
        cmbPriority.getSelectionModel().selectFirst();
        cmbStatus.getSelectionModel().selectFirst();
        dpDeadline.setValue(LocalDate.now());
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
        LocalDate deadline = dpDeadline.getValue();
        String status = cmbStatus.getValue();

        if (title.isEmpty()) {
            // Should not happen as Add button is disabled
            return;
        }

        newTask = new Task(title, description, category, priority, deadline.format(DateTimeFormatter.ISO_DATE), status);
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
