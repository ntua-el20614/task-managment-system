package src;

import Models.User;
import Utils.JsonUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * App.java
 * 
 * Entry point for the MediaLab Assistant JavaFX application.
 */
public class App extends Application {

    private static Stage primaryStage;
    private static User currentUser;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginView();
    }

    /**
     * Shows the Login view.
     */
    public static void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 300); // Adjust dimensions as needed
            primaryStage.setTitle("MediaLab Assistant - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the Main application view after successful login.
     */
    public static void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/MainView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600); // Adjust dimensions as needed
            primaryStage.setTitle("MediaLab Assistant");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the current user after successful login.
     * 
     * @param user The authenticated user.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Gets the current authenticated user.
     * 
     * @return The current user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void stop() {
        if (currentUser != null) {
            JsonUtils.updateUser(currentUser);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static Stage getStage() {
        return primaryStage;
    }

}
