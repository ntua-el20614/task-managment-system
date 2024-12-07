package backend.services;

import backend.models.Task;
import backend.models.Status;
import backend.utils.JsonUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing tasks.
 */
public class TaskService {
    private List<Task> tasks;

    /**
     * Constructor that initializes the task list by loading from JSON.
     */
    public TaskService() {
        this.tasks = JsonUtils.loadTasks();
        updateTaskStatuses(); // Initialize task statuses based on deadlines
    }

    /**
     * Updates the statuses of all tasks based on their deadlines.
     * If a task's deadline has passed and it's not completed, its status is set to DELAYED.
     * This method is package-private to allow testing.
     */
    void updateTaskStatuses() {
        LocalDate today = LocalDate.now();
        boolean updated = false;
        for (Task task : tasks) {
            if (task.getStatus() != Status.COMPLETED && task.getDeadline().isBefore(today)) {
                if (task.getStatus() != Status.DELAYED) {
                    task.setStatus(Status.DELAYED);
                    updated = true;
                }
            }
        }
        if (updated) {
            JsonUtils.saveTasks(tasks);
        }
    }

    /**
     * Adds a new task to the list and saves it to JSON.
     *
     * @param task The task to be added.
     */
    public void addTask(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        if (task.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past.");
        }
        tasks.add(task);
        JsonUtils.saveTasks(tasks);
    }

    /**
     * Modifies an existing task identified by its title.
     *
     * @param updatedTask The task with updated details.
     */
    public void modifyTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            Task existingTask = tasks.get(i);
            if (existingTask.getTitle().equalsIgnoreCase(updatedTask.getTitle())) {
                tasks.set(i, updatedTask);
                JsonUtils.saveTasks(tasks);
                return;
            }
        }
        throw new IllegalArgumentException("Task with title '" + updatedTask.getTitle() + "' not found.");
    }

    /**
     * Deletes a task from the list and saves the updated list to JSON.
     *
     * @param task The task to be deleted.
     */
    public void deleteTask(Task task) {
        boolean removed = tasks.remove(task);
        if (removed) {
            JsonUtils.saveTasks(tasks);
        } else {
            throw new IllegalArgumentException("Task not found and cannot be deleted.");
        }
    }

    /**
     * Retrieves all tasks.
     *
     * @return A list of all tasks.
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Searches for tasks based on title, category, and priority.
     * Any of the parameters can be null, indicating that the filter should not be applied.
     *
     * @param title    The title to search for (partial matches allowed).
     * @param category The category to filter by.
     * @param priority The priority to filter by.
     * @return A list of tasks matching the search criteria.
     */
    public List<Task> searchTasks(String title, String category, String priority) {
        return tasks.stream()
                .filter(task -> (title == null || task.getTitle().toLowerCase().contains(title.toLowerCase())))
                .filter(task -> (category == null || task.getCategory().getName().equalsIgnoreCase(category)))
                .filter(task -> (priority == null || task.getPriority().getName().equalsIgnoreCase(priority)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a task by its title.
     *
     * @param title The title of the task.
     * @return The task if found, otherwise null.
     */
    public Task getTaskByTitle(String title) {
        return tasks.stream()
                .filter(task -> task.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    // Additional methods (e.g., handling reminders) can be added here as needed.
}
