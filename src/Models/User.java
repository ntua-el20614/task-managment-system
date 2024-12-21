package Models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String passwordHash;
    private List<Task> tasks;
    private List<String> categories;
    private List<String> priorities;

    // Constructors
    public User() {
        this.tasks = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.priorities = new ArrayList<>();
    }

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.tasks = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.priorities = new ArrayList<>();
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<String> priorities) {
        this.priorities = priorities;
    }

    // Task Management Methods
    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void removeTask(Task task) {
        this.tasks.remove(task);
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTitle().equals(updatedTask.getTitle())) { // Consider using a unique ID
                tasks.set(i, updatedTask);
                break;
            }
        }
    }

    // Category Management Methods
    public void addCategory(String category) {
        this.categories.add(category);
    }

    public void removeCategory(String category) {
        this.categories.remove(category);
        // Additionally, remove or update tasks associated with this category
        tasks.removeIf(task -> task.getCategory().equals(category));
    }

    public void updateCategory(String oldCategory, String newCategory) {
        int index = categories.indexOf(oldCategory);
        if (index != -1) {
            categories.set(index, newCategory);
            // Update tasks associated with this category
            for (Task task : tasks) {
                if (task.getCategory().equals(oldCategory)) {
                    task.setCategory(newCategory);
                }
            }
        }
    }

    // Priority Management Methods
    public void addPriority(String priority) {
        this.priorities.add(priority);
    }

    public void removePriority(String priority) {
        if (!priority.equals("Default")) {
            this.priorities.remove(priority);
            // Assign default priority to tasks with this priority
            for (Task task : tasks) {
                if (task.getPriority().equals(priority)) {
                    task.setPriority("Default");
                }
            }
        }
    }

    public void updatePriority(String oldPriority, String newPriority) {
        if (!oldPriority.equals("Default")) {
            int index = priorities.indexOf(oldPriority);
            if (index != -1) {
                priorities.set(index, newPriority);
                // Update tasks associated with this priority
                for (Task task : tasks) {
                    if (task.getPriority().equals(oldPriority)) {
                        task.setPriority(newPriority);
                    }
                }
            }
        }
    }
}
