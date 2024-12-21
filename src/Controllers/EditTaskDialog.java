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
 * EditTaskDialog.java
 * 
 * Represents the Edit Task dialog.
 */
public class EditTaskDialog extends Dialog<Task> {

    private User currentUser;
    private Task taskToEdit;

    public EditTaskDialog(User user, Task task) {
        this.currentUser = user;
        this.taskToEdit = task;

        setTitle("Edit Task");
        initModality(Modality.APPLICATION_MODAL);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EditTaskDialog.fxml"));
            Parent root = loader.load();

            EditTaskDialogController controller = loader.getController();
            controller.setTask(currentUser, taskToEdit);

            getDialogPane().setContent(root);

            // Convert the result to an updated Task when the Save button is clicked
            setResultConverter(dialogButton -> {
                if ("Save".equals(dialogButton.getText())) {
                    return controller.getUpdatedTask().orElse(null);
                }
                return null;
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
