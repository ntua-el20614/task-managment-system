package backend.services;

import backend.models.Category;
import backend.models.Priority;
import backend.models.Task;
import backend.models.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TaskService.
 */
class TaskServiceTest {

    private TaskService taskService;
    private CategoryService categoryService;
    private PriorityService priorityService;

    /**
     * Sets up the services before each test.
     */
    @BeforeEach
    void setUp() {
        priorityService = new PriorityService();
        categoryService = new CategoryService();
        taskService = new TaskService();

        // Clear existing tasks to ensure test isolation
        for (Task task : taskService.getAllTasks()) {
            taskService.deleteTask(task);
        }

        // Clear existing categories and priorities
        for (Category category : categoryService.getAllCategories()) {
            categoryService.deleteCategory(category.getName(), taskService);
        }

        for (Priority priority : priorityService.getAllPriorities()) {
            if (!priority.getName().equalsIgnoreCase("Default")) { // Preserve Default priority
                priorityService.deletePriority(priority.getName(), taskService);
            }
        }
    }

    /**
     * Tests adding a new task.
     */
    @Test
    void testAddTask() {
        Category category = new Category("Test Category");
        categoryService.addCategory(category);

        Priority priority = new Priority("Test Priority");
        priorityService.addPriority(priority);

        Task task = new Task("Test Task", "Task Description", category, priority, LocalDate.now().plusDays(5));
        taskService.addTask(task);

        assertTrue(taskService.getAllTasks().contains(task), "Task should be added successfully.");
    }

    /**
     * Tests modifying an existing task.
     */
    @Test
    void testModifyTask() {
        Category category = new Category("Modify Category");
        categoryService.addCategory(category);

        Priority priority = new Priority("Modify Priority");
        priorityService.addPriority(priority);

        Task task = new Task("Modify Task", "Initial Description", category, priority, LocalDate.now().plusDays(3));
        taskService.addTask(task);

        // Modify task description
        task.setDescription("Updated Description");
        taskService.modifyTask(task);

        Task modifiedTask = taskService.getAllTasks().stream()
                .filter(t -> t.getTitle().equalsIgnoreCase("Modify Task"))
                .findFirst()
                .orElse(null);

        assertNotNull(modifiedTask, "Modified task should exist.");
        assertEquals("Updated Description", modifiedTask.getDescription(), "Task description should be updated.");
    }

    /**
     * Tests deleting a task.
     */
    @Test
    void testDeleteTask() {
        Category category = new Category("Delete Category");
        categoryService.addCategory(category);

        Priority priority = new Priority("Delete Priority");
        priorityService.addPriority(priority);

        Task task = new Task("Delete Task", "Delete Description", category, priority, LocalDate.now().plusDays(2));
        taskService.addTask(task);

        taskService.deleteTask(task);

        assertFalse(taskService.getAllTasks().contains(task), "Task should be deleted successfully.");
    }

    /**
     * Tests updating task statuses to DELAYED if deadlines have passed.
     */
    @Test
    void testUpdateTaskStatusToDelayed() {
        Category category = new Category("Status Category");
        categoryService.addCategory(category);

        Priority priority = new Priority("Status Priority");
        priorityService.addPriority(priority);

        Task task = new Task("Delayed Task", "Check status update", category, priority, LocalDate.now().minusDays(1));
        taskService.addTask(task);

        // Call the package-private method to update statuses
        taskService.updateTaskStatuses();

        Task updatedTask = taskService.getAllTasks().stream()
                .filter(t -> t.getTitle().equalsIgnoreCase("Delayed Task"))
                .findFirst()
                .orElse(null);

        assertNotNull(updatedTask, "Task should exist.");
        assertEquals(Status.DELAYED, updatedTask.getStatus(), "Task status should be updated to DELAYED.");
    }

    /**
     * Tests searching for tasks based on title, category, and priority.
     */
    @Test
    void testSearchTasks() {
        Category workCategory = new Category("Work");
        Category personalCategory = new Category("Personal");
        categoryService.addCategory(workCategory);
        categoryService.addCategory(personalCategory);

        Priority highPriority = new Priority("High");
        Priority lowPriority = new Priority("Low");
        priorityService.addPriority(highPriority);
        priorityService.addPriority(lowPriority);

        Task task1 = new Task("Complete Report", "Finish the quarterly report", workCategory, highPriority, LocalDate.now().plusDays(2));
        Task task2 = new Task("Buy Groceries", "Milk, Bread, Eggs", personalCategory, lowPriority, LocalDate.now().plusDays(1));
        Task task3 = new Task("Plan Vacation", "Decide on destination and dates", personalCategory, highPriority, LocalDate.now().plusDays(10));

        taskService.addTask(task1);
        taskService.addTask(task2);
        taskService.addTask(task3);

        // Search by title
        List<Task> searchByTitle = taskService.searchTasks("Report", null, null);
        assertEquals(1, searchByTitle.size(), "Should find one task with 'Report' in the title.");
        assertTrue(searchByTitle.contains(task1), "Search result should contain 'Complete Report'.");

        // Search by category
        List<Task> searchByCategory = taskService.searchTasks(null, "Personal", null);
        assertEquals(2, searchByCategory.size(), "Should find two tasks in the 'Personal' category.");
        assertTrue(searchByCategory.contains(task2), "Search result should contain 'Buy Groceries'.");
        assertTrue(searchByCategory.contains(task3), "Search result should contain 'Plan Vacation'.");

        // Search by priority
        List<Task> searchByPriority = taskService.searchTasks(null, null, "High");
        assertEquals(2, searchByPriority.size(), "Should find two tasks with 'High' priority.");
        assertTrue(searchByPriority.contains(task1), "Search result should contain 'Complete Report'.");
        assertTrue(searchByPriority.contains(task3), "Search result should contain 'Plan Vacation'.");

        // Combined search
        List<Task> combinedSearch = taskService.searchTasks("Plan", "Personal", "High");
        assertEquals(1, combinedSearch.size(), "Should find one task matching all criteria.");
        assertTrue(combinedSearch.contains(task3), "Search result should contain 'Plan Vacation'.");
    }
}
