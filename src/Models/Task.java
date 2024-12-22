package Models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Task.java
 * 
 * Represents a task with various attributes.
 */
public class Task {
    private String title;
    private String description;
    private String category;
    private String priority;
    private String deadline; // Format: YYYY-MM-DD
    private String status;
    private List<String> reminders;

    // Constructors
    public Task() {
        this.reminders = new ArrayList<>();
    }

    public Task(String title, String description, String category, String priority, String deadline, String status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status == null ? "Open" : status; // Default to "Open"
        this.reminders = new ArrayList<>();
        validateStatus();
    }

    public void validateStatus() {
        LocalDate today = LocalDate.now();
        LocalDate taskDeadline = LocalDate.parse(this.deadline, DateTimeFormatter.ISO_DATE);
    
        if (!"Completed".equals(this.status) && taskDeadline.isBefore(today)) {
            this.status = "Delayed";
        }
    }
    // Getters and Setters
    // (Ensure all fields have corresponding getters and setters.)

    public String getTitle() {
        return title;
    }

    // ... [Other getters and setters] ...

    public void setTitle(String title) {
        this.title = title;
    }

    // ... [Other setters] ...

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getReminders() {
        return reminders;
    }

    public void setReminders(List<String> reminders) {
        this.reminders = reminders;
    }

    public void addReminder(String reminder) {
        this.reminders.add(reminder);
    }

    public void removeReminder(String reminder) {
        this.reminders.remove(reminder);
    }

    /**
     * Checks if the task is overdue.
     * 
     * @return True if overdue, false otherwise.
     */
    public boolean isOverdue() {
        LocalDate today = LocalDate.now();
        LocalDate taskDeadline = LocalDate.parse(this.deadline, DateTimeFormatter.ISO_DATE);
        return taskDeadline.isBefore(today);
    }

    /**
     * Checks if the task is due within a specified number of days.
     * 
     * @param days Number of days.
     * @return True if due within the specified days, false otherwise.
     */
    public boolean isDueWithinDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate taskDeadline = LocalDate.parse(this.deadline, DateTimeFormatter.ISO_DATE);
        long daysBetween = ChronoUnit.DAYS.between(today, taskDeadline);
        return daysBetween >= 0 && daysBetween <= days;
    }
}
