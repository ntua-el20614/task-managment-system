package backend.services;

import backend.models.Priority;
import backend.models.Task;
import backend.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PriorityService {
    private List<Priority> priorities;
    private final String DEFAULT_PRIORITY = "Default";

    // Constructor
    public PriorityService() {
        this.priorities = JsonUtils.loadPriorities();
        ensureDefaultPriority();
    }

    // Ensure that Default priority exists
    private void ensureDefaultPriority() {
        boolean exists = priorities.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(DEFAULT_PRIORITY));
        if (!exists) {
            priorities.add(new Priority(DEFAULT_PRIORITY));
            JsonUtils.savePriorities(priorities);
        }
    }

    // Add a new priority level
    public void addPriority(Priority priority) {
        if (!priority.getName().equalsIgnoreCase(DEFAULT_PRIORITY)) {
            priorities.add(priority);
            JsonUtils.savePriorities(priorities);
        }
    }

    // Modify an existing priority level
    public void modifyPriority(String oldName, String newName) {
        if (oldName.equalsIgnoreCase(DEFAULT_PRIORITY)) {
            // Do not allow modifying Default priority
            return;
        }
        for (Priority priority : priorities) {
            if (priority.getName().equalsIgnoreCase(oldName)) {
                priority.setName(newName);
                JsonUtils.savePriorities(priorities);
                return;
            }
        }
    }

    // Delete a priority level and reassign tasks
    public void deletePriority(String priorityName, TaskService taskService) {
        if (priorityName.equalsIgnoreCase(DEFAULT_PRIORITY)) {
            // Do not allow deleting Default priority
            return;
        }
        priorities.removeIf(priority -> priority.getName().equalsIgnoreCase(priorityName));
        // Reassign tasks with deleted priority to Default
        List<Task> affectedTasks = taskService.getAllTasks().stream()
                .filter(task -> task.getPriority().getName().equalsIgnoreCase(priorityName))
                .collect(Collectors.toList());
        Priority defaultPriority = getDefaultPriority();
        for (Task task : affectedTasks) {
            task.setPriority(defaultPriority);
            taskService.modifyTask(task);
        }
        JsonUtils.savePriorities(priorities);
    }

    // Retrieve Default Priority
    public Priority getDefaultPriority() {
        return priorities.stream()
                .filter(p -> p.getName().equalsIgnoreCase(DEFAULT_PRIORITY))
                .findFirst()
                .orElse(new Priority(DEFAULT_PRIORITY));
    }

    // Retrieve all priorities
    public List<Priority> getAllPriorities() {
        return new ArrayList<>(priorities);
    }

    // Additional methods as needed
}
