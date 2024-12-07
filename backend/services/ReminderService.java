package backend.services;

import backend.models.Reminder;
import backend.models.Task;
import backend.utils.JsonUtils;

import java.util.List;

public class ReminderService {
    private List<Reminder> reminders;

    // Constructor
    public ReminderService() {
        this.reminders = JsonUtils.loadReminders();
    }

    // Add a new reminder
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
        JsonUtils.saveReminders(reminders);
    }

    // Modify an existing reminder
    public void modifyReminder(Reminder oldReminder, Reminder newReminder) {
        int index = reminders.indexOf(oldReminder);
        if (index != -1) {
            reminders.set(index, newReminder);
            JsonUtils.saveReminders(reminders);
        }
    }

    // Delete a reminder
    public void deleteReminder(Reminder reminder) {
        reminders.remove(reminder);
        JsonUtils.saveReminders(reminders);
    }

    // Retrieve all reminders
    public List<Reminder> getAllReminders() {
        return reminders;
    }

    // Delete reminders associated with a completed task
    public void deleteRemindersByTask(Task task) {
        reminders.removeIf(reminder -> reminder.getTask().equals(task));
        JsonUtils.saveReminders(reminders);
    }

    // Additional methods as needed
}
