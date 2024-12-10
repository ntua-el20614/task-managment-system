package backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {
    private UUID id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;

    @JsonFormat(pattern = "dd/MM/yy")
    private LocalDate completionDeadline;
    private Status status;

    private List<Reminder> reminders;

    // Constructors
    public Task() {
        this.id = UUID.randomUUID();
        this.status = Status.OPEN;
        this.reminders = new ArrayList<>();
    }

    public Task(String title, String description, Category category, Priority priority, LocalDate completionDeadline) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.completionDeadline = completionDeadline;
        this.status = Status.OPEN;
        this.reminders = new ArrayList<>();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    // No setter for ID to prevent modification

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Similarly, getters and setters for other fields

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

    public LocalDate getCompletionDeadline() {
        return completionDeadline;
    }

    public void setCompletionDeadline(LocalDate completionDeadline) {
        this.completionDeadline = completionDeadline;
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

    // Utility methods

    public void addReminder(Reminder reminder) {
        this.reminders.add(reminder);
    }

    public void removeReminder(Reminder reminder) {
        this.reminders.remove(reminder);
    }
}
