#!/bin/bash

# Define paths
JAVA_FX_LIB=./javafx-sdk-23.0.1/lib
CLASSPATH="./lib/*:out"

# Compile all Java files in the frontend folder with Jackson and JavaFX libraries
javac --module-path "$JAVA_FX_LIB" \
      --add-modules javafx.controls,javafx.fxml \
      -cp "./lib/*" \
      -d out \
      $(find ./frontend -name "*.java")

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running the application..."
    # Run the JavaFX application with Jackson and JavaFX libraries
    java --module-path "$JAVA_FX_LIB" \
         --add-modules javafx.controls,javafx.fxml \
         -cp "$CLASSPATH" \
         frontend.MainClass
else
    echo "Compilation failed. Please fix the errors and try again."
fi
