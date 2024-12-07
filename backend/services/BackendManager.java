package backend.services;

import backend.models.Task;

import java.util.List;

/**
 * Manager class that coordinates different services.
 */
public class BackendManager {
    private TaskService taskService;
    private CategoryService categoryService;
    private PriorityService priorityService;
    private ReminderService reminderService;

    /**
     * Constructor that initializes all services.
     */
    public BackendManager() {
        this.taskService = new TaskService();
        this.categoryService = new CategoryService();
        this.priorityService = new PriorityService();
        this.reminderService = new ReminderService();
    }

    // Methods to interact with services

    /**
     * Retrieves all tasks.
     *
     * @return A list of all tasks.
     */
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // Additional methods to manage tasks, categories, priorities, and reminders
}
