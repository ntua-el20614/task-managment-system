// File: src/main/java/frontend/MainClass.java
package frontend;

import backend.storage.DataStore;
import backend.models.Priority;
import backend.models.Task;
import backend.models.Status;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.UUID;

public class MainClass extends Application {

    private DataStore dataStore;
    private ListView<String> taskListView; // Declared as an instance variable

    @Override
    public void start(Stage primaryStage) {
        dataStore = new DataStore();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("Task Management System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button addTaskButton = new Button("Add Task");
        addTaskButton.setOnAction(e -> showAddTaskDialog());

        taskListView = new ListView<>(); // Initialized the ListView
        updateTaskListView();

        root.getChildren().addAll(titleLabel, addTaskButton, taskListView);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Task Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAddTaskDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Enter Task Details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        ComboBox<Priority> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(dataStore.getAllPriorities());

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Completion Deadline");

        dialogContent.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea,
                new Label("Priority:"), priorityComboBox,
                new Label("Completion Deadline:"), deadlinePicker
        );

        dialog.getDialogPane().setContent(dialogContent);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String title = titleField.getText();
                String description = descriptionArea.getText();
                Priority priority = priorityComboBox.getValue();
                LocalDate deadline = deadlinePicker.getValue();

                if (title == null || title.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Title is required.");
                    return null;
                }

                if (priority == null) {
                    priority = dataStore.getAllPriorities()
                            .stream()
                            .filter(p -> p.getId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                            .findFirst()
                            .orElse(null);
                }

                if (deadline == null) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Completion deadline is required.");
                    return null;
                }

                dataStore.addNewTask(title, description, priority.getId(), deadline);
                updateTaskListView();
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateTaskListView() {
        taskListView.getItems().clear();
        for (Task task : dataStore.getAllTasks()) {
            taskListView.getItems().add(task.getTitle() + " - " + task.getStatus());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
