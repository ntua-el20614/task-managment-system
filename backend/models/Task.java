package backend.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a task.
 */
public class Task {
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private LocalDate deadline;
    private Status status;
    private List<Reminder> reminders;

    /**
     * Constructor for creating a new Task.
     *
     * @param title       The title of the task.
     * @param description The description of the task.
     * @param category    The category of the task.
     * @param priority    The priority of the task.
     * @param deadline    The deadline of the task.
     */
    public Task(String title, String description, Category category, Priority priority, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = Status.OPEN; // Default status
        this.reminders = new ArrayList<>();
    }

    /**
     * Default constructor for JSON deserialization.
     */
    public Task() {
        this.reminders = new ArrayList<>();
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past.");
        }
        this.deadline = deadline;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    // Override equals and hashCode for proper comparison in tests

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task)) return false;

        Task other = (Task) obj;
        return title.equalsIgnoreCase(other.title)
                && description.equals(other.description)
                && category.equals(other.category)
                && priority.equals(other.priority)
                && deadline.equals(other.deadline)
                && status == other.status;
    }

    @Override
    public int hashCode() {
        return title.toLowerCase().hashCode();
    }
}
