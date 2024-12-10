// File: src/main/java/backend/storage/DataStore.java
package backend.storage;

import backend.models.Category;
import backend.models.Priority;
import backend.models.Task;
import backend.models.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class DataStore {
    private static final String DATA_DIR = "backend/data";
    private static final String TASKS_FILE = DATA_DIR + "/tasks.json";
    private static final String CATEGORIES_FILE = DATA_DIR + "/categories.json";
    private static final String PRIORITIES_FILE = DATA_DIR + "/priorities.json";

    // Define a fixed UUID for the Default Priority
    private static final UUID DEFAULT_PRIORITY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private ObjectMapper objectMapper;

    private Map<UUID, Task> tasks;
    private Map<UUID, Category> categories;
    private Map<UUID, Priority> priorities;

    public DataStore() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // To handle Java 8 Date/Time

        // Initialize data directories and files
        initializeDataFiles();

        // Load data
        loadCategories();
        loadPriorities();
        loadTasks();

        // Update task statuses based on deadlines
        updateTaskStatuses();
    }

    private void initializeDataFiles() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean dirCreated = dataDir.mkdirs();
            if (dirCreated) {
                System.out.println("Created data directory at: " + DATA_DIR);
            } else {
                System.err.println("Failed to create data directory at: " + DATA_DIR);
            }
        }

        try {
            // Initialize tasks.json
            File tasksFile = new File(TASKS_FILE);
            if (!tasksFile.exists()) {
                tasksFile.createNewFile();
                objectMapper.writeValue(tasksFile, new ArrayList<Task>());
                System.out.println("Initialized tasks.json with empty array.");
            }

            // Initialize categories.json
            File categoriesFile = new File(CATEGORIES_FILE);
            if (!categoriesFile.exists()) {
                categoriesFile.createNewFile();
                objectMapper.writeValue(categoriesFile, new ArrayList<Category>());
                System.out.println("Initialized categories.json with empty array.");
            }

            // Initialize priorities.json
            File prioritiesFile = new File(PRIORITIES_FILE);
            if (!prioritiesFile.exists()) {
                prioritiesFile.createNewFile();
                // Initialize with "Default" priority using fixed UUID
                Priority defaultPriority = new Priority();
                defaultPriority.setId(DEFAULT_PRIORITY_ID);
                defaultPriority.setName("Default Priority");
                objectMapper.writeValue(prioritiesFile, Collections.singletonList(defaultPriority));
                System.out.println("Initialized priorities.json with Default priority.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load Categories
    private void loadCategories() {
        try {
            File categoriesFile = new File(CATEGORIES_FILE);
            List<Category> categoryList = objectMapper.readValue(categoriesFile, new TypeReference<List<Category>>() {});
            categories = new HashMap<>();
            for (Category category : categoryList) {
                categories.put(category.getId(), category);
            }
            System.out.println("Loaded " + categories.size() + " categories.");
        } catch (IOException e) {
            e.printStackTrace();
            categories = new HashMap<>();
        }
    }

    // Save Categories
    private void saveCategories() {
        try {
            File categoriesFile = new File(CATEGORIES_FILE);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(categoriesFile, categories.values());
            System.out.println("Saved " + categories.size() + " categories to categories.json.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load Priorities
    private void loadPriorities() {
        try {
            File prioritiesFile = new File(PRIORITIES_FILE);
            List<Priority> priorityList = objectMapper.readValue(prioritiesFile, new TypeReference<List<Priority>>() {});
            priorities = new HashMap<>();
            for (Priority priority : priorityList) {
                priorities.put(priority.getId(), priority);
            }

            // Ensure Default Priority exists
            if (!priorities.containsKey(DEFAULT_PRIORITY_ID)) {
                Priority defaultPriority = new Priority();
                defaultPriority.setId(DEFAULT_PRIORITY_ID);
                defaultPriority.setName("Default Priority");
                priorities.put(DEFAULT_PRIORITY_ID, defaultPriority);
                savePriorities();
                System.out.println("Added missing Default Priority.");
            }

            System.out.println("Loaded " + priorities.size() + " priorities.");
        } catch (IOException e) {
            e.printStackTrace();
            priorities = new HashMap<>();
            // Initialize Default Priority if loading fails
            Priority defaultPriority = new Priority();
            defaultPriority.setId(DEFAULT_PRIORITY_ID);
            defaultPriority.setName("Default Priority");
            priorities.put(DEFAULT_PRIORITY_ID, defaultPriority);
            savePriorities();
            System.out.println("Initialized priorities with Default Priority due to loading failure.");
        }
    }

    // Save Priorities
    private void savePriorities() {
        try {
            File prioritiesFile = new File(PRIORITIES_FILE);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(prioritiesFile, priorities.values());
            System.out.println("Saved " + priorities.size() + " priorities to priorities.json.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load Tasks
    private void loadTasks() {
        try {
            File tasksFile = new File(TASKS_FILE);
            List<Task> taskList = objectMapper.readValue(tasksFile, new TypeReference<List<Task>>() {});
            tasks = new HashMap<>();
            for (Task task : taskList) {
                tasks.put(task.getId(), task);
            }
            System.out.println("Loaded " + tasks.size() + " tasks.");
        } catch (IOException e) {
            e.printStackTrace();
            tasks = new HashMap<>();
        }
    }

    // Save Tasks
    private void saveTasks() {
        try {
            File tasksFile = new File(TASKS_FILE);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tasksFile, tasks.values());
            System.out.println("Saved " + tasks.size() + " tasks to tasks.json.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Category Operations
    public Collection<Category> getAllCategories() {
        return categories.values();
    }

    public void addCategory(Category category) {
        categories.put(category.getId(), category);
        saveCategories();
    }

    public void updateCategory(Category category) {
        categories.put(category.getId(), category);
        saveCategories();
    }

    public void deleteCategory(UUID categoryId) {
        if (categories.containsKey(categoryId)) {
            categories.remove(categoryId);
            saveCategories();
        } else {
            System.err.println("Category with ID " + categoryId + " does not exist.");
        }
    }

    // Priority Operations
    public Collection<Priority> getAllPriorities() {
        return priorities.values();
    }

    public void addPriority(Priority priority) {
        // Prevent adding another "Default" priority
        if (priority.getName().equalsIgnoreCase("Default")) {
            System.err.println("Cannot add another 'Default' priority.");
            return;
        }
        priorities.put(priority.getId(), priority);
        savePriorities();
    }

    public void updatePriority(Priority priority) {
        // Prevent modifying the "Default" priority
        if (priority.getId().equals(DEFAULT_PRIORITY_ID)) {
            System.err.println("Cannot modify the 'Default' priority.");
            return;
        }
        priorities.put(priority.getId(), priority);
        savePriorities();
    }

    public void deletePriority(UUID priorityId) {
        // Prevent deleting the "Default" priority
        if (priorityId.equals(DEFAULT_PRIORITY_ID)) {
            System.err.println("Cannot delete the 'Default' priority.");
            return;
        }

        if (priorities.containsKey(priorityId)) {
            // Reassign tasks with this priority to "Default"
            Priority defaultPriority = priorities.get(DEFAULT_PRIORITY_ID);
            for (Task task : tasks.values()) {
                if (task.getPriority().getId().equals(priorityId)) {
                    task.setPriority(defaultPriority);
                }
            }
            priorities.remove(priorityId);
            savePriorities();
            saveTasks();
            System.out.println("Deleted Priority with ID " + priorityId + " and reassigned associated tasks to Default Priority.");
        } else {
            System.err.println("Priority with ID " + priorityId + " does not exist.");
        }
    }

    // Task Operations
    public Collection<Task> getAllTasks() {
        return tasks.values();
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        saveTasks();
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            saveTasks();
        } else {
            System.err.println("Task with ID " + task.getId() + " does not exist.");
        }
    }

    public void deleteTask(UUID taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            saveTasks();
        } else {
            System.err.println("Task with ID " + taskId + " does not exist.");
        }
    }

    // Automatic Status Update
    private void updateTaskStatuses() {
        LocalDate today = LocalDate.now();
        boolean updated = false;

        for (Task task : tasks.values()) {
            if (task.getStatus() != Status.COMPLETED && task.getCompletionDeadline().isBefore(today)) {
                if (task.getStatus() != Status.DELAYED) {
                    task.setStatus(Status.DELAYED);
                    updated = true;
                    System.out.println("Task '" + task.getTitle() + "' status updated to DELAYED.");
                }
            }
        }

        if (updated) {
            saveTasks();
        }
    }

    // Example method to create the Default Priority if needed
    public void createDefaultPriority() {
        Priority defaultPriority = new Priority();
        defaultPriority.setId(DEFAULT_PRIORITY_ID); // Fixed UUID
        defaultPriority.setName("Default Priority");
        try {
            savePriority(defaultPriority);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load a single priority (utility method)
    private Priority loadPriorityById(UUID id) throws IOException {
        File prioritiesFile = new File(PRIORITIES_FILE);
        List<Priority> priorityList = objectMapper.readValue(prioritiesFile, new TypeReference<List<Priority>>() {});
        for (Priority priority : priorityList) {
            if (priority.getId().equals(id)) {
                return priority;
            }
        }
        return null;
    }

    // Save a single priority (utility method)
    private void savePriority(Priority priority) throws IOException {
        List<Priority> priorityList = objectMapper.readValue(new File(PRIORITIES_FILE), new TypeReference<List<Priority>>() {});
        priorityList.add(priority);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(PRIORITIES_FILE), priorityList);
    }
}
