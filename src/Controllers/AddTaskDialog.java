package Controllers;

import Models.Task;
import Models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AddTaskDialog extends Stage {

    private final AddTaskDialogController controller;

    public AddTaskDialog(User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddTaskDialog.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.setUser(currentUser); // Pass the user to the controller
            this.setScene(new Scene(root));
            this.setTitle("Add Task");
            this.initModality(Modality.APPLICATION_MODAL); // Make the dialog modal
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load AddTaskDialog.fxml");
        }
    }

    /**
     * Retrieves the newly added task, if any.
     *
     * @return Optional containing the new Task, or empty if no task was added.
     */
    public Optional<Task> getNewTask() {
        return controller.getNewTask();
    }

    /**
     * Returns the controller for this dialog.
     * 
     * @return The AddTaskDialogController instance.
     */
    public AddTaskDialogController getController() {
        return controller;
    }
}
