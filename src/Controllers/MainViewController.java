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
        currentUser = App.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login if user is not set
            App.showLoginView();
            return;
        }

        // Initialize task list
        taskList = FXCollections.observableArrayList(currentUser.getTasks());
        taskListView.setItems(taskList);
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

        // Update overdue tasks
        updateOverdueTasks();

        // Update aggregated information
        updateAggregatedInfo();
    }

    private void updateOverdueTasks() {
        for (Task task : currentUser.getTasks()) {
            if (!task.getStatus().equalsIgnoreCase("Completed")) {
                if (task.isOverdue()) {
                    task.setStatus("Delayed");
                }
            }
        }
        JsonUtils.updateUser(currentUser);
        taskList.setAll(currentUser.getTasks());
    }

    private void updateAggregatedInfo() {
        lblTotalTasks.setText("Total Tasks: " + currentUser.getTasks().size());
        lblCompleted.setText("Completed: " + countTasksByStatus("Completed"));
        lblDelayed.setText("Delayed: " + countTasksByStatus("Delayed"));
        lblUpcoming.setText("Due within 7 days: " + countUpcomingTasks());
    }

    private long countTasksByStatus(String status) {
        return currentUser.getTasks().stream()
                .filter(task -> task.getStatus().equalsIgnoreCase(status))
                .count();
    }

    private long countUpcomingTasks() {
        return currentUser.getTasks().stream()
                .filter(task -> task.isDueWithinDays(7))
                .count();
    }

    @FXML
    private void handleAddTask() {
        AddTaskDialog addTaskDialog = new AddTaskDialog(currentUser);
        Optional<Task> result = addTaskDialog.showAndWait();
        result.ifPresent(task -> {
            currentUser.addTask(task);
            JsonUtils.updateUser(currentUser);
            taskList.add(task);
            updateAggregatedInfo();
        });
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            EditTaskDialog editTaskDialog = new EditTaskDialog(currentUser, selectedTask);
            Optional<Task> result = editTaskDialog.showAndWait();
            result.ifPresent(updatedTask -> {
                currentUser.updateTask(updatedTask);
                JsonUtils.updateUser(currentUser);
                taskList.set(taskList.indexOf(selectedTask), updatedTask);
                updateAggregatedInfo();
            });
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
                currentUser.removeTask(selectedTask);
                JsonUtils.updateUser(currentUser);
                taskList.remove(selectedTask);
                updateAggregatedInfo();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to delete.");
        }
    }

    @FXML
    private void handleLogout() {
        App.setCurrentUser(null);
        App.showLoginView();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
