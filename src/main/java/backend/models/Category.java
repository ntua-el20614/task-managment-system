package backend.models;

import java.util.UUID;

public class Category {
    private UUID id;
    private String name;

    // Constructors
    public Category() {
        this.id = UUID.randomUUID();
    }

    public Category(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    // No setter for ID to prevent modification

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    
}
