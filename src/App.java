package src;

import Models.Task;
import Models.User;
import Utils.JsonUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static User currentUser;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Validate delayed tasks at program start
        if (currentUser != null) {
            validateDelayedTasks();
        }

        showLoginView();
    }

    /**
     * Checks and marks tasks as delayed for the current user.
     */
    private void validateDelayedTasks() {
        if (currentUser != null) {
            for (Task task : currentUser.getTasks()) {
                task.validateStatus();
            }
            JsonUtils.updateUser(currentUser); // Persist the changes
        }
    }

    public static void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 343);
            primaryStage.setTitle("MediaLab Assistant - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/Views/MainWrapper.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("MediaLab Assistant");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public static void refreshCurrentUser() {
        if (currentUser != null) {
            User updatedUser = JsonUtils.findUser(currentUser.getUsername());
            if (updatedUser != null) {
                currentUser = updatedUser;
            }
        }
    }
    /*
    @Override
    public void stop() {
        if (currentUser != null) {
            JsonUtils.updateUser(currentUser);
        }
    }
    */

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage() {
        return primaryStage;
    }
}
