package backend.models;

public class Priority {
    private String id;
    private String name;

    // Parameterized Constructor
    public Priority(String name) {
        this.id = generateUniqueId();
        this.name = name;
    }

    // Default Constructor (required for JSON deserialization)
    public Priority() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    // No setter for ID to prevent modification

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Helper method to generate a unique ID (UUID)
    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
