package backend.models;

public class Category {
    private String id;
    private String name;

    // Parameterized Constructor
    public Category(String name) {
        this.id = generateUniqueId();
        this.name = name;
    }

    // Default Constructor (required for JSON deserialization)
    public Category() {}

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
