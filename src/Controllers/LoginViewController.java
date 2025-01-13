package Controllers;

import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import src.App;

import java.util.List;
import java.util.stream.Collectors;

public class LoginViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private Label lblMessage;

    @FXML
    public void initialize() {
        System.out.println("LoginViewController initialized.");
    }

    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            return;
        }

        User user = AuthController.login(username, password);
        if (user != null) {
            App.setCurrentUser(user);

            // Validate tasks for delayed status
            validateDelayedTasks(user);

            // Show delayed tasks dialog
            showDelayedTasks(user);

            App.showMainView(); // Navigate to main application
        } else {
            lblMessage.setText("Invalid username or password.");
        }
    }

    @FXML
    private void handleSignupButtonAction() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            return;
        }

        boolean success = AuthController.register(username, password);
        if (success) {
            lblMessage.setText("Registration successful. You can now log in.");
        } else {
            lblMessage.setText("Username already exists.");
        }
    }

    private void validateDelayedTasks(User user) {
        for (Task task : user.getTasks()) {
            task.validateStatus();
        }
        JsonUtils.updateUser(user); // Persist the updated tasks
    }

    private void showDelayedTasks(User user) {
        List<Task> delayedTasks = user.getTasks().stream()
                .filter(task -> "Delayed".equals(task.getStatus()))
                .collect(Collectors.toList());

        if (!delayedTasks.isEmpty()) {
            StringBuilder message = new StringBuilder("You have delayed tasks:\n\n");
            for (Task task : delayedTasks) {
                message.append("• ").append(task.getTitle())
                        .append(" (Due: ").append(task.getDeadline()).append(")\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Delayed Tasks");
            alert.setHeaderText("Attention!");
            alert.setContentText(message.toString());
            alert.showAndWait();
        }
    }
}
