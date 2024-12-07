package backend.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import backend.models.Category;
import backend.models.Priority;
import backend.models.Reminder;
import backend.models.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling JSON read and write operations.
 */
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    // Define paths relative to the project root
    private static final String DATA_DIR = "backend/utils/data/";
    private static final String TASK_FILE = DATA_DIR + "task.json";
    private static final String CATEGORY_FILE = DATA_DIR + "category.json";
    private static final String PRIORITY_FILE = DATA_DIR + "priority.json";
    private static final String REMINDER_FILE = DATA_DIR + "reminder.json";

    /**
     * Loads tasks from the JSON file.
     *
     * @return A list of tasks.
     */
    public static List<Task> loadTasks() {
        return loadData(TASK_FILE, new TypeReference<List<Task>>() {});
    }

    /**
     * Saves tasks to the JSON file.
     *
     * @param tasks The list of tasks to save.
     */
    public static void saveTasks(List<Task> tasks) {
        saveData(TASK_FILE, tasks);
    }

    /**
     * Loads categories from the JSON file.
     *
     * @return A list of categories.
     */
    public static List<Category> loadCategories() {
        return loadData(CATEGORY_FILE, new TypeReference<List<Category>>() {});
    }

    /**
     * Saves categories to the JSON file.
     *
     * @param categories The list of categories to save.
     */
    public static void saveCategories(List<Category> categories) {
        saveData(CATEGORY_FILE, categories);
    }

    /**
     * Loads priorities from the JSON file.
     * Ensures that a "Default" priority exists.
     *
     * @return A list of priorities.
     */
    public static List<Priority> loadPriorities() {
        List<Priority> priorities = loadData(PRIORITY_FILE, new TypeReference<List<Priority>>() {});
        if (priorities.isEmpty()) {
            priorities.add(new Priority("Default"));
            savePriorities(priorities);
        }
        return priorities;
    }

    /**
     * Saves priorities to the JSON file.
     *
     * @param priorities The list of priorities to save.
     */
    public static void savePriorities(List<Priority> priorities) {
        saveData(PRIORITY_FILE, priorities);
    }

    /**
     * Loads reminders from the JSON file.
     *
     * @return A list of reminders.
     */
    public static List<Reminder> loadReminders() {
        return loadData(REMINDER_FILE, new TypeReference<List<Reminder>>() {});
    }

    /**
     * Saves reminders to the JSON file.
     *
     * @param reminders The list of reminders to save.
     */
    public static void saveReminders(List<Reminder> reminders) {
        saveData(REMINDER_FILE, reminders);
    }

    /**
     * Generic method to load data from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @param typeRef  The type reference for deserialization.
     * @param <T>      The type of data to load.
     * @return A list of data objects.
     */
    private static <T> List<T> loadData(String filePath, TypeReference<List<T>> typeRef) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Generic method to save data to a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @param data     The list of data objects to save.
     * @param <T>      The type of data to save.
     */
    private static <T> void saveData(String filePath, List<T> data) {
        File file = new File(filePath);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
