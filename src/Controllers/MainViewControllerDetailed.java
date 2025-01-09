package Controllers;

import src.App;
import Models.Task;
import Models.User;
import Utils.JsonUtils;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainViewControllerDetailed implements Initializable {

    @FXML
    private Label lblTotalTasks;

    @FXML
    private Label lblCompleted;

    @FXML
    private Label lblDelayed;

    @FXML
    private Label lblUpcoming;

    @FXML
    private ListView<Task> openList;

    @FXML
    private ListView<Task> inProgressList;

    @FXML
    private ListView<Task> postponedList;

    @FXML
    private ListView<Task> completedList;

    @FXML
    private ListView<Task> delayedList;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnSettings;

    private User currentUser;
    private ObservableList<Task> taskList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = App.getCurrentUser();
        if (currentUser == null) {
            App.showLoginView();
            return;
        }

        taskList = FXCollections.observableArrayList(currentUser.getTasks());

        initializeColumn(openList, "Open");
        initializeColumn(inProgressList, "In Progress");
        initializeColumn(postponedList, "Postponed");
        initializeColumn(completedList, "Completed");
        initializeColumn(delayedList, "Delayed");

        reloadTasks();
    }

private void initializeColumn(ListView<Task> column, String status) {
    column.setItems(FXCollections.observableArrayList());
    column.setCellFactory(param -> new ListCell<Task>() {
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setText(null);
            } else {
                setText(task.getTitle());
            }
        }
    });

    column.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) { // Double-click detected
            Task selectedTask = column.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                openEditTaskView(selectedTask);
            }
        }
    });

    column.setOnDragDetected(event -> {
        Task selectedTask = column.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Dragboard db = column.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedTask.getId());
            db.setContent(content);
            event.consume();
        }
    });

    column.setOnDragOver(event -> {
        if (event.getGestureSource() != column && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    });

    column.setOnDragDropped(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasString()) {
            String taskId = db.getString();
            Task draggedTask = taskList.stream()
                    .filter(task -> task.getId().equals(taskId))
                    .findFirst()
                    .orElse(null);

            if (draggedTask != null) {
                draggedTask.setStatus(status);
                JsonUtils.updateUser(currentUser);
                reloadTasks();
            }
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
    });
}

    private void openEditTaskView(Task selectedTask) {
        EditTaskDialog editTaskDialog = new EditTaskDialog(currentUser, selectedTask);
        editTaskDialog.showAndWait(); // Show the dialog (does not return a value)
        Optional<Task> result = editTaskDialog.getUpdatedTask(); // Retrieve the updated task after the dialog closes
        if (result.isPresent()) {
            reloadTasks(); // Refresh the task lists
        }
    }



    public class EditTaskDialog extends Stage {
        private final EditTaskDialogController controller;

        public EditTaskDialog(User currentUser, Task taskToEdit) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EditTaskDialog.fxml"));
                Parent root = loader.load();
                controller = loader.getController();
                controller.setTask(currentUser, taskToEdit);
                this.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load EditTaskDialog.fxml");
            }
        }

        public Optional<Task> getUpdatedTask() {
            return controller.getUpdatedTask();
        }
    }



    private void reloadTasks() {
        // Refresh the user data from the JSON file
        User refreshedUser = JsonUtils.findUser(currentUser.getUsername());
        if (refreshedUser != null) {
            currentUser = refreshedUser; // Update current user with refreshed data
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh user data.");
            return;
        }

        // Update the task list from the current user's tasks
        taskList.setAll(currentUser.getTasks());
        System.out.println("Reloaded " + taskList.size() + " tasks.");

        // Update the labels with refreshed counts
        lblTotalTasks.setText("Total Tasks: " + taskList.size());
        lblCompleted.setText("Completed: " + countTasksByStatus("Completed"));
        lblDelayed.setText("Delayed: " + countTasksByStatus("Delayed"));
        lblUpcoming.setText("Due within 7 days: " + countUpcomingTasks());

        // Refresh each ListView with filtered tasks
        openList.getItems().setAll(filterTasksByStatus("Open"));
        inProgressList.getItems().setAll(filterTasksByStatus("In Progress"));
        postponedList.getItems().setAll(filterTasksByStatus("Postponed"));
        completedList.getItems().setAll(filterTasksByStatus("Completed"));
        delayedList.getItems().setAll(filterTasksByStatus("Delayed"));
    }



    private ObservableList<Task> filterTasksByStatus(String status) {
        return FXCollections.observableArrayList(
                taskList.stream()
                        .filter(task -> task.getStatus().equalsIgnoreCase(status))
                        .toList()
        );
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
    private void handleAddTask() {
        try {
            // Reload user data to ensure the latest categories and priorities
            User updatedUser = JsonUtils.findUser(currentUser.getUsername());
            if (updatedUser != null) {
                currentUser = updatedUser; // Update the current user
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user data.");
                return;
            }

            // Create and show the AddTaskDialog
            AddTaskDialog addTaskDialog = new AddTaskDialog(currentUser);
            addTaskDialog.showAndWait(); // Display the dialog

            // Check if a new task was added
            Optional<Task> newTask = addTaskDialog.getNewTask();
            if (newTask.isPresent()) {
                currentUser.getTasks().add(newTask.get()); // Add the new task to the current user's tasks
                JsonUtils.updateUser(currentUser); // Persist the changes to the JSON file
                reloadTasks(); // Refresh the task list to show the new task
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Add Task dialog.");
            e.printStackTrace();
        }
    }

    
    @FXML
    private void handleEditTask() {
        Task selectedTask = getSelectedTask();
        if (selectedTask != null) {
            openEditTaskView(selectedTask); // Use the existing method
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to edit.");
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = getSelectedTask();
        if (selectedTask != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Task");
            confirm.setHeaderText("Are you sure you want to delete the selected task?");
            confirm.setContentText(selectedTask.getTitle());
    
            Optional<ButtonType> response = confirm.showAndWait();
            if (response.isPresent() && response.get() == ButtonType.OK) {
                // Remove the task from both the taskList and currentUser's tasks
                taskList.remove(selectedTask);
                currentUser.getTasks().removeIf(task -> task.getId().equals(selectedTask.getId()));
    
                // Persist the changes to the JSON file
                JsonUtils.updateUser(currentUser);
    
                // Reload the lists to reflect the changes
                reloadTasks();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to delete.");
        }
    }


    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing");
        reloadTasks();
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

    private Task getSelectedTask() {
    // Check all task lists for a selected task
    if (openList.getSelectionModel().getSelectedItem() != null) {
        return openList.getSelectionModel().getSelectedItem();
    } else if (inProgressList.getSelectionModel().getSelectedItem() != null) {
        return inProgressList.getSelectionModel().getSelectedItem();
    } else if (postponedList.getSelectionModel().getSelectedItem() != null) {
        return postponedList.getSelectionModel().getSelectedItem();
    } else if (completedList.getSelectionModel().getSelectedItem() != null) {
        return completedList.getSelectionModel().getSelectedItem();
    } else if (delayedList.getSelectionModel().getSelectedItem() != null) {
        return delayedList.getSelectionModel().getSelectedItem();
    }
    return null; // Return null if no task is selected
    }

}
