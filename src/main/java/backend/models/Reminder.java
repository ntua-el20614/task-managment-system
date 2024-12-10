package backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.UUID;

public class Reminder {
    private UUID id;
    private ReminderType type;

    @JsonFormat(pattern = "dd/MM/yy")
    private LocalDate specificDate; // Only for SPECIFIC_DATE

    @JsonFormat(pattern = "dd/MM/yy")
    private LocalDate reminderDate;

    // Constructors
    public Reminder() {
        this.id = UUID.randomUUID();
    }

    public Reminder(ReminderType type, LocalDate completionDeadline, LocalDate specificDate) {
        this.id = UUID.randomUUID();
        this.type = type;
        if (type == ReminderType.SPECIFIC_DATE) {
            this.specificDate = specificDate;
            this.reminderDate = specificDate;
        } else {
            calculateReminderDate(completionDeadline);
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    // No setter for ID to prevent modification

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    // Utility method to calculate reminderDate based on type and deadline
    @JsonIgnore
    public void calculateReminderDate(LocalDate completionDeadline) {
        switch (this.type) {
            case ONE_DAY_BEFORE:
                this.reminderDate = completionDeadline.minusDays(1);
                break;
            case ONE_WEEK_BEFORE:
                this.reminderDate = completionDeadline.minusWeeks(1);
                break;
            case ONE_MONTH_BEFORE:
                this.reminderDate = completionDeadline.minusMonths(1);
                break;
            default:
                // Do nothing for SPECIFIC_DATE
                break;
        }
    }
}
