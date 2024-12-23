package Controllers;
import src.App;

import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import src.App;


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
        
        App.getStage().setOnCloseRequest(event -> reloadTasks());
        
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
        // Reload user data to ensure we have the latest categories and priorities
        User updatedUser = JsonUtils.findUser(currentUser.getUsername());
        if (updatedUser != null) {
            currentUser = updatedUser; // Update the current user
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user data.");
            return;
        }
    
        AddTaskDialog addTaskDialog = new AddTaskDialog(currentUser);
        AddTaskDialogController controller = addTaskDialog.getController();
    
        // Ensure the dialog dynamically loads the latest categories and priorities
        controller.setUser(currentUser);
    
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
                // Replace the old task with the updated one in the user's task list
                currentUser.getTasks().replaceAll(task -> 
                    task.equals(selectedTask) ? updatedTask : task
                );
                
                JsonUtils.updateUser(currentUser); // Persist changes to the JSON file
                reloadTasks(); // Reload tasks to ensure the ListView reflects the changes
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


    @FXML
    private void handleSettings() {
        try {
            // Load the Settings view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/SettingsView.fxml"));
            Parent root = loader.load();

            // Configure the settings modal
            Stage settingsStage = new Stage();
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setTitle("Settings");
            settingsStage.setScene(new Scene(root));
            settingsStage.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the settings modal.");
            e.printStackTrace();
        }
    }

    /**
     * Handles refresh.
     */
    @FXML
    private void handleRefresh() {
        reloadTasks();
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
