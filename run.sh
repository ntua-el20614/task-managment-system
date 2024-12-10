#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "Starting Maven build and execution..."

# Clean and compile the Maven project
mvn clean compile

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running the application..."
    # Run the JavaFX application using Maven's exec plugin
    mvn exec:java
else
    echo "Compilation failed. Please fix the errors and try again."
    exit 1
fi
