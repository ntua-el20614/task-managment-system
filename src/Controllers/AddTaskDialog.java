package Controllers;

import Models.Task;
import Models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.Optional;

/**
 * AddTaskDialog.java
 * 
 * Represents the Add Task dialog.
 */
public class AddTaskDialog extends Dialog<Task> {

    private User currentUser;

    public AddTaskDialog(User user) {
        this.currentUser = user;

        setTitle("Add New Task");
        initModality(Modality.APPLICATION_MODAL);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddTaskDialog.fxml"));
            Parent root = loader.load();

            AddTaskDialogController controller = loader.getController();
            controller.setUser(currentUser);

            getDialogPane().setContent(root);

            // Convert the result to a Task when the Add button is clicked
            setResultConverter(dialogButton -> {
                if ("Add".equals(dialogButton.getText())) {
                    return controller.getNewTask().orElse(null);
                }
                return null;
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
