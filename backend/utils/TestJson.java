package backend.utils;

import backend.models.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Utility class for testing JSON operations.
 */
public class TestJson {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reads tasks from a JSON string for testing purposes.
     *
     * @param json The JSON string representing a list of tasks.
     * @return A list of Task objects.
     * @throws IOException If an I/O error occurs.
     */
    public static List<Task> readTasksFromJson(String json) throws IOException {
        return mapper.readValue(json, new TypeReference<List<Task>>() {});
    }

    /**
     * Converts a list of tasks to a JSON string for testing purposes.
     *
     * @param tasks The list of Task objects.
     * @return A JSON string representing the list of tasks.
     * @throws IOException If an I/O error occurs.
     */
    public static String writeTasksToJson(List<Task> tasks) throws IOException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tasks);
    }
}
