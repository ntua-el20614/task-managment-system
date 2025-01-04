package Controllers;

import Models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import src.App;

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

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        System.out.println("LoginViewController initialized.");
        if (lblMessage != null) {
            System.out.println("Label initialized successfully.");
        } else {
            System.out.println("Label not initialized.");
        }
    }

    /**
     * Handles the Login button action.
     */
    @FXML
    private void handleLoginButtonAction() {
        System.out.println("Login button clicked."); // Debugging statement
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            return;
        }

        // Authenticate the user
        User user = AuthController.login(username, password);
        if (user != null) {
            App.setCurrentUser(user);
            App.showMainView();
        } else {
            lblMessage.setText("Invalid username or password.");
        }
    }

    /**
     * Handles the Signup button action.
     */
    @FXML
    private void handleSignupButtonAction() {
        System.out.println("Signup button clicked."); // Debugging statement
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
}
