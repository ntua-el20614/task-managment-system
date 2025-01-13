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
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.Optional;
import java.net.URL;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainViewListController implements Initializable {

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
    // Truncate strings to avoid excessively long inputs
    String truncatedCategory = task.getCategory().length() > 45 ? task.getCategory().substring(0, 45) : task.getCategory();
    String truncatedStatus = "[" + task.getStatus() + "]";
    String truncatedDescription = task.getDescription().length() > 45 ? task.getDescription().substring(0, 45) + "..." : task.getDescription();

    // Calculate padding and ensure a minimum width of 1
    int categoryPadding = Math.max(1, 50 - truncatedCategory.length());
    String paddedCategory = String.format("%-" + categoryPadding + "s", truncatedCategory);

    int statusPadding = Math.max(1, 45 - truncatedStatus.length());
    String paddedStatus = String.format("%-" + statusPadding + "s", truncatedStatus);

    // Format the final string
    setText(String.format("%-30s %s %s %s", task.getTitle(), paddedCategory, paddedStatus, truncatedDescription));

}




            }
        });

        // load the buttons
        initializeButtonIcons();

        // Load tasks for the current user
        reloadTasks();
    }

    private void reloadTasks() {
        User updatedUser = JsonUtils.findUser(currentUser.getUsername());
        if (updatedUser != null) {
            currentUser = updatedUser; // Replace current user with updated data
            taskList.setAll(currentUser.getTasks());
            // System.out.println("I got " + taskList.size() + " tasks");

            updateAggregatedInfo();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reload tasks. User data could not be found.");
        }
    }

    private void updateAggregatedInfo() {
        lblTotalTasks.setText("Total Tasks: " + taskList.size());
        lblCompleted.setText("Completed: " + countTasksByStatus("Completed"));
        lblDelayed.setText("Delayed: " + countTasksByStatus("Delayed"));
        lblUpcoming.setText("Due within 7 days: " + countUpcomingTasks());
    }

    private long countTasksByStatus(String status) {
        return taskList.stream()
                .filter(task -> task.getStatus().equalsIgnoreCase(status))
                .count();
    }

    private long countUpcomingTasks() {
        return taskList.stream()
                .filter(task -> task.isDueWithinDays(7))
                .count();
    }

    @FXML
    private ImageView addIcon, editIcon, deleteIcon, refreshIcon, settingsIcon;

    private void initializeButtonIcons() {
        addIcon.setImage(new Image(App.class.getResource("/Media/add.png").toExternalForm()));
        editIcon.setImage(new Image(App.class.getResource("/Media/edit.png").toExternalForm()));
        deleteIcon.setImage(new Image(App.class.getResource("/Media/delete.png").toExternalForm()));
        refreshIcon.setImage(new Image(App.class.getResource("/Media/refresh.png").toExternalForm()));
        settingsIcon.setImage(new Image(App.class.getResource("/Media/settings.png").toExternalForm()));
    }


    @FXML
    private void handleAddTask() {
        try {
            // Reload user data to ensure we have the latest categories and priorities
            User updatedUser = JsonUtils.findUser(currentUser.getUsername());
            if (updatedUser != null) {
                currentUser = updatedUser; // Update the current user
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user data.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddTaskDialog.fxml"));
            Parent root = loader.load();

            // Retrieve the controller and set the user
            AddTaskDialogController controller = loader.getController();
            controller.setUser(currentUser);

            // Show the Add Task dialog
            Stage stage = new Stage();
            stage.setTitle("Add Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload tasks after adding a new task
            reloadTasks();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Add Task dialog.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EditTaskDialog.fxml"));
                Parent root = loader.load();

                // Retrieve the controller from the loader
                EditTaskDialogController controller = loader.getController();
                controller.setTask(currentUser, selectedTask);

                // Create and show the dialog
                Stage stage = new Stage();
                stage.setTitle("Edit Task");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

                // Get the updated task
                Optional<Task> updatedTask = controller.getUpdatedTask();
                if (updatedTask.isPresent()) {
                    reloadTasks(); // Refresh tasks to reflect changes
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Edit Task dialog.");
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to edit.");
        }
    }

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
                // Remove the task from both currentUser and taskList
                currentUser.getTasks().removeIf(task -> task.getId().equals(selectedTask.getId()));
                JsonUtils.updateUser(currentUser); // Persist the changes

                // Refresh the ObservableList
                reloadTasks(); // Reload tasks to ensure consistency
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to delete.");
        }
    }


    
    @FXML
    private void handleRefresh() {
        reloadTasks(); // Refresh the task list
    }
    

    @FXML
    private void handleSettings() {
        try {
            // Load the Settings view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/SettingsView.fxml"));
            Parent root = loader.load();

            // Configure the settings modal
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setScene(new Scene(root));
            settingsStage.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the Settings dialog.");
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
