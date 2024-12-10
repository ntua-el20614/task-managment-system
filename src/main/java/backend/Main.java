package backend;

import backend.models.Category;
import backend.models.Priority;
import backend.models.Task;
import backend.storage.DataStore;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        DataStore dataStore = new DataStore();

        // Create a new category
        Category workCategory = new Category("Work");
        dataStore.addCategory(workCategory);
        System.out.println("Added Category: " + workCategory.getName());

        // Create a new priority
        Priority highPriority = new Priority("High");
        dataStore.addPriority(highPriority);
        System.out.println("Added Priority: " + highPriority.getName());

        // Create a new task with a future deadline
        Task futureTask = new Task(
                "Plan Team Meeting",
                "Organize the quarterly team meeting.",
                workCategory,
                highPriority,
                LocalDate.of(2024, 12, 25)
        );
        dataStore.addTask(futureTask);
        System.out.println("Added Task: " + futureTask.getTitle());

        // Create a new task with a past deadline to test automatic status update
        Task overdueTask = new Task(
                "Submit Tax Forms",
                "Complete and submit all tax-related documents.",
                workCategory,
                highPriority,
                LocalDate.of(2024, 1, 15) // Ensure this date is before the current date to trigger DELAYED status
        );
        dataStore.addTask(overdueTask);
        System.out.println("Added Task: " + overdueTask.getTitle());

        // Retrieve and display all tasks
        System.out.println("\nAll Tasks:");
        for (Task t : dataStore.getAllTasks()) {
            System.out.println("Title: " + t.getTitle() + ", Status: " + t.getStatus());
        }
    }
}
