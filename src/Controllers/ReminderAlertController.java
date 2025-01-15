package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ReminderAlertController {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblDeadline;

    @FXML
    private Label lblReminderDate;

    @FXML
    private Button btnClose;

    public void setReminderDetails(String title, String description, String deadline, String reminderDate) {
        lblTitle.setText(title);
        lblDescription.setText(description);
        lblDeadline.setText("Deadline: " + deadline);
        lblReminderDate.setText("Reminder Date: " + reminderDate);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
