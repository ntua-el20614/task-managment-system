package Controllers;

import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import src.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    @FXML
    private Label lblTotalTasks;

    @FXML
    private Label lblCompleted;

    @FXML
    private Label lblDelayed;

    @FXML
    private Label lblUpcoming;

    @FXML
    private ListView<Task> taskListView;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnLogout;

    private User currentUser;
    private ObservableList<Task> taskList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Retrieve the current user
        currentUser = App.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login if no user is logged in
            App.showLoginView();
            return;
        }

        // Initialize the task list and ListView
        taskList = FXCollections.observableArrayList();
        taskListView.setItems(taskList);

        // Set custom cell factory for task display
        taskListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                } else {
                    setText(task.getTitle() + " [" + task.getStatus() + "]");
                }
            }
        });

        // Load tasks for the current user
        reloadTasks();
    }

    /**
     * Reloads tasks from the current user's data in the JSON file and updates the ListView.
     */
    private void reloadTasks() {
        // Reload the current user's data from the JSON file
        User updatedUser = JsonUtils.findUser(currentUser.getUsername());
        if (updatedUser != null) {
            currentUser = updatedUser; // Replace current user with updated data
            taskList.setAll(currentUser.getTasks()); // Update the ObservableList with tasks from the file
            updateAggregatedInfo(); // Update task statistics
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reload tasks. User data could not be found.");
        }
    }

    /**
     * Updates the labels showing aggregated task information.
     */
    private void updateAggregatedInfo() {
        lblTotalTasks.setText("Total Tasks: " + taskList.size());
        lblCompleted.setText("Completed: " + countTasksByStatus("Completed"));
        lblDelayed.setText("Delayed: " + countTasksByStatus("Delayed"));
        lblUpcoming.setText("Due within 7 days: " + countUpcomingTasks());
    }

    /**
     * Counts tasks by their status.
     *
     * @param status the status to count
     * @return the number of tasks with the specified status
     */
    private long countTasksByStatus(String status) {
        return taskList.stream()
                .filter(task -> task.getStatus().equalsIgnoreCase(status))
                .count();
    }

    /**
     * Counts tasks due within the next 7 days.
     *
     * @return the number of tasks due within 7 days
     */
    private long countUpcomingTasks() {
        return taskList.stream()
                .filter(task -> task.isDueWithinDays(7))
                .count();
    }

    /**
     * Handles adding a new task.
     */
    @FXML
    private void handleAddTask() {
        AddTaskDialog addTaskDialog = new AddTaskDialog(currentUser);
        AddTaskDialogController controller = addTaskDialog.getController();
        controller.setOnTaskAddedCallback(ignored -> reloadTasks()); // Set the reload callback
    
        addTaskDialog.showAndWait(); // Show the dialog
    }



    /**
     * Handles editing the selected task.
     */
    @FXML
    private void handleEditTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            EditTaskDialog editTaskDialog = new EditTaskDialog(currentUser, selectedTask);
            Optional<Task> result = editTaskDialog.showAndWait();
            if (result.isPresent()) {
                Task updatedTask = result.get();
                currentUser.updateTask(updatedTask); // Update the task in the user object
                JsonUtils.updateUser(currentUser); // Save changes to the JSON file
                reloadTasks(); // Reload tasks to ensure the ListView reflects the change
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to edit.");
        }
    }

    /**
     * Handles deleting the selected task.
     */
    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Task");
            confirm.setHeaderText("Are you sure you want to delete the selected task?");
            confirm.setContentText(selectedTask.getTitle());

            Optional<ButtonType> response = confirm.showAndWait();
            if (response.isPresent() && response.get() == ButtonType.OK) {
                currentUser.removeTask(selectedTask); // Remove the task from the user object
                JsonUtils.updateUser(currentUser); // Save changes to the JSON file
                reloadTasks(); // Reload tasks to ensure the ListView reflects the change
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to delete.");
        }
    }

    /**
     * Handles user logout.
     */
    @FXML
    private void handleLogout() {
        App.setCurrentUser(null);
        App.showLoginView(); // Redirect to login
    }

    /**
     * Shows an alert dialog with the specified type, title, and message.
     *
     * @param type    the alert type
     * @param title   the alert title
     * @param message the alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
