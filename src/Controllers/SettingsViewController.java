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

import java.util.Optional;

public class SettingsViewController {

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNewCategory;

    @FXML
    private Button btnNewPriority;

    @FXML
    private Button btnClose;

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = App.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user is logged in.");
            closeWindow();
        }
    }

    /**
     * Handles logging out the user and returning to the login view.
     */
    @FXML
    private void handleLogout() {
        App.setCurrentUser(null);
        App.showLoginView(); // Redirect to login
        closeWindow(); // Close the settings modal
    }

    /**
     * Handles managing categories.
     */
    @FXML
    private void handleNewCategory() {
        
        App.refreshCurrentUser();
        currentUser = App.getCurrentUser();

        ObservableList<String> categories = FXCollections.observableArrayList(currentUser.getCategories());
        manageItems("Category", categories, "Default Category");

        // Update user's categories after the dialog
        currentUser.getCategories().clear();
        currentUser.getCategories().addAll(categories);
        JsonUtils.updateUser(currentUser);
    }

    /**
     * Handles managing priorities.
     */
    @FXML
    private void handleNewPriority() {
        
        App.refreshCurrentUser();
        currentUser = App.getCurrentUser();

        ObservableList<String> priorities = FXCollections.observableArrayList(currentUser.getPriorities());
        manageItems("Priority", priorities, "Default");

        // Update user's priorities after the dialog
        currentUser.getPriorities().clear();
        currentUser.getPriorities().addAll(priorities);
        JsonUtils.updateUser(currentUser);
    }

    /**
     * Manages categories or priorities by allowing the user to view, add, or remove them.
     *
     * @param itemType    The type of item (Category or Priority).
     * @param items       The list of items (categories or priorities).
     * @param defaultItem The default item that cannot be deleted.
     */
    private void manageItems(String itemType, ObservableList<String> items, String defaultItem) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Manage " + itemType + "s");
        dialog.setHeaderText("View, add, or remove " + itemType.toLowerCase() + "s");

        // Set up dialog components
        ListView<String> listView = new ListView<>(items);
        listView.setPrefHeight(200);
        TextField inputField = new TextField();
        inputField.setPromptText("New " + itemType);
        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");

        // Disable remove button if no item is selected
        removeButton.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());

        // Add button action
        addButton.setOnAction(event -> {
            String newItem = inputField.getText().trim();
            if (newItem.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "The " + itemType.toLowerCase() + " name cannot be empty.");
            } else if (items.contains(newItem)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate " + itemType, "This " + itemType.toLowerCase() + " already exists.");
            } else {
                items.add(newItem);
                inputField.clear();
            }
        });

        // Remove button action
        removeButton.setOnAction(event -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem.equals(defaultItem)) {
                showAlert(Alert.AlertType.WARNING, "Restricted", "The default " + itemType.toLowerCase() + " cannot be deleted.");
            } else {
                items.remove(selectedItem);
            }
        });

        // Set up dialog layout
        VBox content = new VBox(10);
        content.getChildren().addAll(listView, inputField, new HBox(10, addButton, removeButton));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Show the dialog
        dialog.showAndWait();
    }

    /**
     * Closes the settings modal.
     */
    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
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
