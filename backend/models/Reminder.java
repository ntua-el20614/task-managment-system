package backend.models;

import java.time.LocalDate;

public class Reminder {
    private String id;
    private ReminderType type;
    private LocalDate date; // Used only if type is SPECIFIC_DATE
    private String taskId; // Associated Task ID

    // Parameterized Constructor for Predefined Types
    public Reminder(ReminderType type, String taskId, LocalDate taskDeadline) {
        this.id = generateUniqueId();
        this.type = type;
        this.taskId = taskId;
        this.date = calculateDate(taskDeadline);
    }

    // Parameterized Constructor for Specific Date
    public Reminder(LocalDate date, String taskId) {
        this.id = generateUniqueId();
        this.type = ReminderType.SPECIFIC_DATE;
        this.date = date;
        this.taskId = taskId;
    }

    // Default Constructor (required for JSON deserialization)
    public Reminder() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    // No setter for ID to prevent modification

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    // Helper method to calculate reminder date based on type
    private LocalDate calculateDate(LocalDate taskDeadline) {
        switch (type) {
            case ONE_DAY_BEFORE:
                return taskDeadline.minusDays(1);
            case ONE_WEEK_BEFORE:
                return taskDeadline.minusWeeks(1);
            case ONE_MONTH_BEFORE:
                return taskDeadline.minusMonths(1);
            default:
                return null;
        }
    }

    // Helper method to generate a unique ID (UUID)
    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        if (type == ReminderType.SPECIFIC_DATE) {
            return "Reminder on " + date;
        } else {
            return "Reminder " + type.toString().replace("_", " ").toLowerCase();
        }
    }
}
