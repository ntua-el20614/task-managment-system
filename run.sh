#!/bin/bash

# Compile all Java files in the frontend folder with Jackson and JavaFX libraries
javac --module-path ./javafx-sdk-23.0.1/lib --add-modules javafx.controls,javafx.fxml -cp "./lib/*" -d out $(find ./frontend -name "*.java")

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running the application..."
    # Run the JavaFX application with Jackson and JavaFX libraries
    java --module-path ./javafx-sdk-23.0.1/lib --add-modules javafx.controls,javafx.fxml -cp "./lib/*:out" frontend.MainClass
else
    echo "Compilation failed. Please fix the errors and try again."
fi

