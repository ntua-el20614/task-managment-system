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
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.collections.ListChangeListener;
import java.time.LocalDate;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
// import images
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

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

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private ComboBox<String> cmbPriority;


    @FXML
    private Button btnClearFilters;

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

    // Ensure ObservableList
    ObservableList<String> categories = FXCollections.observableArrayList(currentUser.getCategories());
    ObservableList<String> priorities = FXCollections.observableArrayList(currentUser.getPriorities());

    // Initialize multi-select dropdowns
    initializeMultiSelectDropdown(cmbCategory, categories);
    initializeMultiSelectDropdown(cmbPriority, priorities);

    txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterTasks());

    btnClearFilters.setOnAction(event -> clearFilters());

    initializeColumn(openList, "Open");
    initializeColumn(inProgressList, "In Progress");
    initializeColumn(postponedList, "Postponed");
    initializeColumn(completedList, "Completed");
        initializeColumn(delayedList, "Delayed");
        initializeButtonIcons();

        reloadTasks();

        cmbCategory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    // Display prompt text when there's no selection
                    setText(cmbCategory.getPromptText());
                    setStyle("-fx-text-fill: white;");  // Set prompt text color to white
                } else {
                    setText(item);
                    setStyle("");  // Reset style for non-prompt items or apply desired style
                }
            }
        });

        cmbPriority.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(cmbPriority.getPromptText());
                    setStyle("-fx-text-fill: white;");
                } else {
                    setText(item);
                    setStyle("");
                }
            }
        });
    }



    private void initializeMultiSelectDropdown(ComboBox<String> comboBox, ObservableList<String> items) {
        // Create an ObservableList to track selected items
        ObservableList<String> selectedItems = FXCollections.observableArrayList();
        comboBox.getProperties().put("selectedItems", selectedItems); // Store selected items in ComboBox properties

        // Set ComboBox items
        comboBox.setItems(items);

        // Override default dropdown behavior
    comboBox.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
        // If the user clicks on the arrow button (the ComboBox field),
    // then toggle the open/close state.  
    // If the user clicks inside the list cells, keep it open.

    // Check if the click is on the list cell area or the arrow area:
        Node node = event.getPickResult().getIntersectedNode();
        if (node instanceof Labeled || node instanceof ListCell) {
            // The user clicked on a list item; keep it open
            event.consume();
        } else {
            // The user clicked on the arrow or "button" area; toggle open/close
            if (comboBox.isShowing()) {
                comboBox.hide();
            } else {
                comboBox.show();
            }
            event.consume();
        }
    });


        // Use a custom cell factory for rendering checkboxes
        comboBox.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    String item = getItem();
                    if (checkBox.isSelected()) {
                        selectedItems.add(item); // Add to selected items
                    } else {
                        selectedItems.remove(item); // Remove from selected items
                    }
                    filterTasks(); // Trigger filtering
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    checkBox.setText(item);
                    checkBox.setSelected(selectedItems.contains(item));
                    setGraphic(checkBox);
                }
            }
        });

        // Update the ComboBox button cell to display selected items
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || selectedItems.isEmpty()) {
                    setText(comboBox.getPromptText());
                } else {
                    setText(getEllipsizedText(String.join(", ", selectedItems), 20)); // Limit length
                }
            }
        });
    }

    private String getEllipsizedText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text; // No need to truncate
        }
        return text.substring(0, maxLength - 3) + "..."; // Add ellipsis
    }


    private void filterTasks() {
        String searchText = txtSearch.getText().toLowerCase();

        // Retrieve selected items from ComboBoxes
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedCategories = (ObservableList<String>) cmbCategory.getProperties().get("selectedItems");
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedPriorities = (ObservableList<String>) cmbPriority.getProperties().get("selectedItems");

        ObservableList<Task> filteredTasks = FXCollections.observableArrayList(
                taskList.stream()
                        .filter(task -> 
                            (searchText.isEmpty() || task.getTitle().toLowerCase().contains(searchText)) &&
                            (selectedCategories.isEmpty() || selectedCategories.contains(task.getCategory())) &&
                            (selectedPriorities.isEmpty() || selectedPriorities.contains(task.getPriority()))
                        )
                        .toList()
        );

        updateListView(filteredTasks);
    }


    private String getSelectedItemsAsString(ComboBox<String> comboBox) {
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedItems = (ObservableList<String>) comboBox.getProperties().get("selectedItems");
        return String.join(", ", selectedItems);
    }



    private void updateListView(ObservableList<Task> filteredTasks) {
        openList.getItems().setAll(filteredTasks.stream().filter(task -> task.getStatus().equalsIgnoreCase("Open")).toList());
        inProgressList.getItems().setAll(filteredTasks.stream().filter(task -> task.getStatus().equalsIgnoreCase("In Progress")).toList());
        postponedList.getItems().setAll(filteredTasks.stream().filter(task -> task.getStatus().equalsIgnoreCase("Postponed")).toList());
        completedList.getItems().setAll(filteredTasks.stream().filter(task -> task.getStatus().equalsIgnoreCase("Completed")).toList());
        delayedList.getItems().setAll(filteredTasks.stream().filter(task -> task.getStatus().equalsIgnoreCase("Delayed")).toList());
    }

    private void clearFilters() {
        txtSearch.clear();

        // Clear both the selection model and the 'selectedItems' list for categories
        cmbCategory.getSelectionModel().clearSelection();
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedCategories = (ObservableList<String>) cmbCategory.getProperties().get("selectedItems");
        selectedCategories.clear();

        // Clear both the selection model and the 'selectedItems' list for priorities
        cmbPriority.getSelectionModel().clearSelection();
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedPriorities = (ObservableList<String>) cmbPriority.getProperties().get("selectedItems");
        selectedPriorities.clear();

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
                // Check if the status is "Completed"
                if ("Completed".equals(status)) {
                    // Clear all reminders for the task
                    if (draggedTask.getReminders() != null) {
                        draggedTask.getReminders().clear();
                    }
                }

                // Update the task's status
                draggedTask.setStatus(status);

                // Persist the changes to the user data
                JsonUtils.updateUser(currentUser);

                // Reload and filter tasks
                reloadTasks();
                filterTasks();
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
            filterTasks(); // Reapply filters
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
        // System.out.println("Reloaded " + taskList.size() + " tasks.");

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
                filterTasks(); // Reapply filters
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
                filterTasks();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to delete.");
        }
    }


    @FXML
    private void handleRefresh() {
        // System.out.println("Refreshing");
        reloadTasks();
        filterTasks();
    }

    @FXML
    private void handleSettings() {
    try {
        // Load the Settings view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/SettingsView.fxml"));
        Parent root = loader.load();

        // Retrieve the SettingsViewController
        SettingsViewController controller = loader.getController();

        // Set the callback to reload tasks when settings change
        controller.setOnSettingsChangedCallback(ignored -> {
            reloadTasks();
            filterTasks();
        });

        // Configure the settings modal
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Settings");
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.setScene(new Scene(root));

        // Show the settings modal and wait for it to close
        settingsStage.showAndWait();

        // Explicitly reload tasks after the settings modal is closed
        reloadTasks();
        filterTasks();
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the Settings dialog.");
        e.printStackTrace();
    }
    }


    @FXML
    private ImageView addIcon, editIcon, deleteIcon, refreshIcon, settingsIcon;

    private void initializeButtonIcons() {
        try {
            addIcon.setImage(new Image(App.class.getResource("/Media/add.png").toExternalForm()));
            editIcon.setImage(new Image(App.class.getResource("/Media/edit.png").toExternalForm()));
            deleteIcon.setImage(new Image(App.class.getResource("/Media/delete.png").toExternalForm()));
            refreshIcon.setImage(new Image(App.class.getResource("/Media/refresh.png").toExternalForm()));
            settingsIcon.setImage(new Image(App.class.getResource("/Media/settings.png").toExternalForm()));
        } catch (NullPointerException e) {
            System.err.println("Error loading button icons: " + e.getMessage());
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
