// File: src/main/java/backend/models/Priority.java
package backend.models;

import java.util.UUID;

public class Priority {
    private UUID id;
    private String name;

    // Default Constructor
    public Priority() {
        this.id = UUID.randomUUID(); // Automatically generate a UUID
    }

    // Parameterized Constructor
    public Priority(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    // Getter for id
    public UUID getId() {
        return id;
    }

    // Setter for id
    public void setId(UUID id) {
        this.id = id;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Optional: Override toString(), equals(), and hashCode()
    @Override
    public String toString() {
        return "Priority{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    // Implement equals() and hashCode() if necessary
}
