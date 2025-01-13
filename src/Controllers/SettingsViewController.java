package Controllers;

import src.App;
import Models.User;
import Utils.JsonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import java.util.Optional;
import Models.Task;
public class SettingsViewController {

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnClose;

    @FXML
    private ListView<String> listViewCategories;

    @FXML
    private ListView<String> listViewPriorities;

    @FXML
    private TextField inputNewCategory;

    @FXML
    private TextField inputNewPriority;

    @FXML
    private Button btnAddCategory;

    @FXML
    private Button btnRenameCategory;

    @FXML
    private Button btnDeleteCategory;

    @FXML
    private Button btnAddPriority;

    @FXML
    private Button btnRenamePriority;

    @FXML
    private Button btnDeletePriority;

    private User currentUser;

    private Consumer<Void> onSettingsChangedCallback;

    @FXML
    public void initialize() {
        currentUser = App.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user is logged in.");
            closeWindow();
            return;
        }

        // Initialize category and priority lists
        refreshLists();
        setUpButtonBindings();
    }

    private void refreshLists() {
        ObservableList<String> categories = FXCollections.observableArrayList(currentUser.getCategories());
        ObservableList<String> priorities = FXCollections.observableArrayList(currentUser.getPriorities());

        listViewCategories.setItems(categories);
        listViewPriorities.setItems(priorities);
    }

    private void setUpButtonBindings() {
        btnRenameCategory.disableProperty().bind(listViewCategories.getSelectionModel().selectedItemProperty().isNull());
        btnDeleteCategory.disableProperty().bind(listViewCategories.getSelectionModel().selectedItemProperty().isNull());

        btnRenamePriority.disableProperty().bind(listViewPriorities.getSelectionModel().selectedItemProperty().isNull());
        btnDeletePriority.disableProperty().bind(listViewPriorities.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void handleAddCategory() {
        String newCategory = inputNewCategory.getText().trim();
        if (newCategory.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Category name cannot be empty.");
        } else if (listViewCategories.getItems().contains(newCategory)) {
            showAlert(Alert.AlertType.WARNING, "Duplicate Category", "This category already exists.");
        } else {
            listViewCategories.getItems().add(newCategory);
            inputNewCategory.clear();
            updateUserCategories();
        }
    }

    @FXML
    private void handleRenameCategory() {
    String selectedCategory = listViewCategories.getSelectionModel().getSelectedItem();
    TextInputDialog dialog = new TextInputDialog(selectedCategory);
    dialog.setTitle("Rename Category");
    dialog.setHeaderText("Rename the selected category");
    dialog.setContentText("New name:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(newName -> {
        if (!newName.isEmpty() && !listViewCategories.getItems().contains(newName)) {
            // Update category in the list
            listViewCategories.getItems().set(listViewCategories.getSelectionModel().getSelectedIndex(), newName);

            // Update tasks with the old category to the new category
            currentUser.getTasks().forEach(task -> {
                if (task.getCategory().equals(selectedCategory)) {
                    task.setCategory(newName);
                }
            });

            // Update the user data and persist changes
            updateUserCategories();
            JsonUtils.updateUser(currentUser);
        } else {
            showAlert(Alert.AlertType.WARNING, "Invalid Name", "The new category name is invalid or already exists.");
        }
    });
    }

    @FXML
    private void handleDeleteCategory() {
    String selectedCategory = listViewCategories.getSelectionModel().getSelectedItem();

    if (selectedCategory == null) {
        showAlert(Alert.AlertType.WARNING, "No Category Selected", "Please select a category to delete.");
        return;
    }

    // Collect tasks associated with the selected category
    ObservableList<Task> tasksToDelete = FXCollections.observableArrayList(
        currentUser.getTasks().stream()
            .filter(task -> task.getCategory().equals(selectedCategory))
            .toList()
    );

    // Create a confirmation dialog with a list of tasks to delete
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Delete Category");
    confirm.setHeaderText("Are you sure you want to delete the category: " + selectedCategory + "?");
    confirm.setContentText("The following tasks will also be deleted:\n\n" +
        tasksToDelete.stream()
            .map(Task::getTitle) // Assuming Task has a getTitle() method
            .reduce("", (a, b) -> a + "- " + b + "\n")
    );

    Optional<ButtonType> response = confirm.showAndWait();
    if (response.isPresent() && response.get() == ButtonType.OK) {
        // Remove the category
        listViewCategories.getItems().remove(selectedCategory);

        // Remove tasks associated with the deleted category
        currentUser.getTasks().removeIf(task -> task.getCategory().equals(selectedCategory));

        // Persist changes
        updateUserCategories();
        JsonUtils.updateUser(currentUser);

        // Invoke the callback to reload tasks in the main view
        if (onSettingsChangedCallback != null) {
            onSettingsChangedCallback.accept(null);
        }

        // Show success message
        showAlert(Alert.AlertType.INFORMATION, "Category Deleted", 
            "The category and its associated tasks have been successfully deleted.");
    }
    }




    @FXML
    private void handleAddPriority() {
        String newPriority = inputNewPriority.getText().trim();
        if (newPriority.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Priority name cannot be empty.");
        } else if (listViewPriorities.getItems().contains(newPriority)) {
            showAlert(Alert.AlertType.WARNING, "Duplicate Priority", "This priority already exists.");
        } else {
            listViewPriorities.getItems().add(newPriority);
            inputNewPriority.clear();
            updateUserPriorities();
        }
    }

    @FXML
    private void handleRenamePriority() {
    String selectedPriority = listViewPriorities.getSelectionModel().getSelectedItem();

    if (selectedPriority == null) {
        showAlert(Alert.AlertType.WARNING, "No Priority Selected", "Please select a priority to rename.");
        return;
    }

    TextInputDialog dialog = new TextInputDialog(selectedPriority);
    dialog.setTitle("Rename Priority");
    dialog.setHeaderText("Rename the selected priority");
    dialog.setContentText("New name:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(newName -> {
        if (!newName.isEmpty() && !listViewPriorities.getItems().contains(newName)) {
            // Update the priority in the list
            listViewPriorities.getItems().set(listViewPriorities.getSelectionModel().getSelectedIndex(), newName);

            // Update tasks with the old priority to the new priority
            currentUser.getTasks().forEach(task -> {
                if (task.getPriority().equals(selectedPriority)) {
                    task.setPriority(newName);
                }
            });

            // Persist changes
            updateUserPriorities();
            JsonUtils.updateUser(currentUser);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Priority Renamed",
                "The priority and its associated tasks have been successfully renamed.");

            // Trigger the callback to refresh tasks in the main view
            if (onSettingsChangedCallback != null) {
                onSettingsChangedCallback.accept(null);
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Invalid Name", "The new priority name is invalid or already exists.");
        }
    });
    }


    @FXML
    private void handleDeletePriority() {
    String selectedPriority = listViewPriorities.getSelectionModel().getSelectedItem();

    if (selectedPriority == null) {
        showAlert(Alert.AlertType.WARNING, "No Priority Selected", "Please select a priority to delete.");
        return;
    }

    if (selectedPriority.equals("Default")) {
        showAlert(Alert.AlertType.WARNING, "Cannot Delete Default Priority", "The default priority cannot be deleted.");
        return;
    }

    // Confirmation dialog
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Delete Priority");
    confirm.setHeaderText("Are you sure you want to delete the priority: " + selectedPriority + "?");
    confirm.setContentText("All tasks with this priority will be reassigned to 'Default'.");

    Optional<ButtonType> response = confirm.showAndWait();
    if (response.isPresent() && response.get() == ButtonType.OK) {
        // Remove the priority from the list
        listViewPriorities.getItems().remove(selectedPriority);

        // Update tasks to reassign the priority to "Default"
        currentUser.getTasks().forEach(task -> {
            if (task.getPriority().equals(selectedPriority)) {
                task.setPriority("Default");
            }
        });

        // Persist changes
        updateUserPriorities();
        JsonUtils.updateUser(currentUser);

        // Show success message
        showAlert(Alert.AlertType.INFORMATION, "Priority Deleted",
            "The priority has been deleted and all associated tasks have been reassigned to 'Default'.");

        // Trigger the callback to refresh tasks in the main view
        if (onSettingsChangedCallback != null) {
            onSettingsChangedCallback.accept(null);
        }
    }
    }


    private void updateUserCategories() {
        currentUser.getCategories().clear();
        currentUser.getCategories().addAll(listViewCategories.getItems());
        JsonUtils.updateUser(currentUser);
    }

    private void updateUserPriorities() {
        currentUser.getPriorities().clear();
        currentUser.getPriorities().addAll(listViewPriorities.getItems());
        JsonUtils.updateUser(currentUser);
    }

    @FXML
    private void handleLogout() {
        App.setCurrentUser(null);
        App.showLoginView();
        closeWindow();
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        if (onSettingsChangedCallback != null) {
            onSettingsChangedCallback.accept(null);
        }
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }



    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * Sets the callback to be executed when settings change.
     *
     * @param onSettingsChangedCallback The callback function.
     */
    public void setOnSettingsChangedCallback(Consumer<Void> onSettingsChangedCallback) {
        this.onSettingsChangedCallback = onSettingsChangedCallback;
    }

}
