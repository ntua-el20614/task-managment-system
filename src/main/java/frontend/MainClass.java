package frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Main class for the JavaFX frontend.
 */
public class MainClass extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hello, Task Management System!");
        Scene scene = new Scene(label, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
