package Controllers;

import src.App;
import Models.Task;
import Models.User;
import Utils.JsonUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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
    private Button btnLogout;  // Or rename to what you need

    // NEW: Search & Filters Controls
    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private ComboBox<String> cmbPriority;

    @FXML
    private Button btnClearFilters;

    @FXML
    private ImageView addIcon, editIcon, deleteIcon, refreshIcon, settingsIcon;

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

        // Initialize the main task list
        taskList = FXCollections.observableArrayList();
        taskListView.setItems(taskList);

        // Set custom cell factory for task display
        taskListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                } else {
                    // Truncate strings to avoid excessively long inputs
                    String truncatedCategory = task.getCategory().length() > 45
                            ? task.getCategory().substring(0, 45)
                            : task.getCategory();
                    String truncatedStatus = "[" + task.getStatus() + "]";
                    String truncatedDescription = task.getDescription().length() > 45
                            ? task.getDescription().substring(0, 45) + "..."
                            : task.getDescription();

                    // Calculate padding and ensure a minimum width of 1
                    int categoryPadding = Math.max(1, 50 - truncatedCategory.length());
                    String paddedCategory = String.format("%-" + categoryPadding + "s", truncatedCategory);

                    int statusPadding = Math.max(1, 45 - truncatedStatus.length());
                    String paddedStatus = String.format("%-" + statusPadding + "s", truncatedStatus);

                    // Format the final string
                    setText(String.format(
                            "%-30s %s %s %s",
                            task.getTitle(), paddedCategory, paddedStatus, truncatedDescription
                    ));
                }
            }
        });

        // Add double-click event listener to open Edit Task view
        taskListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click detected
                Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    handleEditTask();
                }
            }
        });

        // Initialize icons
        initializeButtonIcons();

        // Initialize the multi-select ComboBoxes
        initializeFilters();

        // Load tasks for the current user
        reloadTasks();
    }

    /**
     * Initialize the search and multi-select dropdown filters.
     */
    private void initializeFilters() {
        // 1) Build the multi-select property lists for categories/priorities
        ObservableList<String> categories = FXCollections.observableArrayList(currentUser.getCategories());
        ObservableList<String> priorities = FXCollections.observableArrayList(currentUser.getPriorities());

        // 2) Set up multi-select for each ComboBox
        initializeMultiSelectDropdown(cmbCategory, categories);
        initializeMultiSelectDropdown(cmbPriority, priorities);

        // 3) Listen for search text changes and re-filter
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterTasks());

        // 4) Handle Clear Filters button
        btnClearFilters.setOnAction(event -> clearFilters());
    }

    /**
     * Converts a standard ComboBox into a multi-check ComboBox with an internal list of selected items.
     */
    private void initializeMultiSelectDropdown(ComboBox<String> comboBox, ObservableList<String> items) {
        // Create an ObservableList to track selected items
        ObservableList<String> selectedItems = FXCollections.observableArrayList();
        comboBox.getProperties().put("selectedItems", selectedItems); // Store in ComboBox properties

        // Set ComboBox items
        comboBox.setItems(items);

        // Override default dropdown behavior so it only closes on arrow clicks
        comboBox.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
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

        // Use a custom cell factory for checkboxes
        comboBox.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(evt -> {
                    String item = getItem();
                    if (checkBox.isSelected()) {
                        selectedItems.add(item);
                    } else {
                        selectedItems.remove(item);
                    }
                    filterTasks(); // Re-filter whenever a selection changes
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

        // Update the ComboBox's button cell to display the selected items
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || selectedItems.isEmpty()) {
                    setText(comboBox.getPromptText());
                } else {
                    setText(getEllipsizedText(String.join(", ", selectedItems), 20));
                }
            }
        });
    }

    private String getEllipsizedText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Filter tasks by search text, selected categories, and selected priorities.
     */
    private void filterTasks() {
        String searchText = txtSearch.getText().trim().toLowerCase();

        @SuppressWarnings("unchecked")
        ObservableList<String> selectedCategories =
                (ObservableList<String>) cmbCategory.getProperties().get("selectedItems");
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedPriorities =
                (ObservableList<String>) cmbPriority.getProperties().get("selectedItems");

        // Filter tasks from the current user's entire task list
        ObservableList<Task> filtered = FXCollections.observableArrayList(
            currentUser.getTasks().stream()
                .filter(task ->
                    (searchText.isEmpty() ||
                     task.getTitle().toLowerCase().contains(searchText)) &&
                    (selectedCategories.isEmpty() ||
                     selectedCategories.contains(task.getCategory())) &&
                    (selectedPriorities.isEmpty() ||
                     selectedPriorities.contains(task.getPriority()))
                )
                .toList()
        );

        // Update the ListView
        taskList.setAll(filtered);
        updateAggregatedInfo();
    }

    /**
     * Clear filters: reset the text field and uncheck everything.
     */
    private void clearFilters() {
        txtSearch.clear();

        // Clear category selections
        cmbCategory.getSelectionModel().clearSelection();
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedCategories =
            (ObservableList<String>) cmbCategory.getProperties().get("selectedItems");
        selectedCategories.clear();

        // Clear priority selections
        cmbPriority.getSelectionModel().clearSelection();
        @SuppressWarnings("unchecked")
        ObservableList<String> selectedPriorities =
            (ObservableList<String>) cmbPriority.getProperties().get("selectedItems");
        selectedPriorities.clear();

        // Reload tasks to show everything again
        reloadTasks();
    }

    /**
     * Reload tasks from storage and refresh the UI.
     */
    private void reloadTasks() {
        User updatedUser = JsonUtils.findUser(currentUser.getUsername());
        if (updatedUser != null) {
            currentUser = updatedUser; // Replace current user with updated data
            taskList.setAll(currentUser.getTasks());
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

    /** Icon initialization. */
    private void initializeButtonIcons() {
        addIcon.setImage(new Image(App.class.getResource("/Media/add.png").toExternalForm()));
        editIcon.setImage(new Image(App.class.getResource("/Media/edit.png").toExternalForm()));
        deleteIcon.setImage(new Image(App.class.getResource("/Media/delete.png").toExternalForm()));
        refreshIcon.setImage(new Image(App.class.getResource("/Media/refresh.png").toExternalForm()));
        settingsIcon.setImage(new Image(App.class.getResource("/Media/settings.png").toExternalForm()));
    }

    // ===================
    //   Button Handlers
    // ===================

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

            // Retrieve the SettingsViewController
            SettingsViewController controller = loader.getController();

            // Set the callback to reload tasks when settings change
            controller.setOnSettingsChangedCallback(ignored -> reloadTasks());

            // Configure the settings modal
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setScene(new Scene(root));

            // Show the settings modal and wait for it to close
            settingsStage.showAndWait();

            // Explicitly reload tasks after the settings modal is closed
            reloadTasks();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the Settings dialog.");
            e.printStackTrace();
        }
    }

    // ===================
    //      Utilities
    // ===================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
