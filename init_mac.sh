#!/bin/bash

# init.sh - Unzips JavaFX SDK if needed and runs the Task Management System

# Path to JavaFX SDK zip
ZIP_PATH=./lib/javafx-sdk-23.0.1.zip

# Path to target unzipped folder
UNZIPPED_PATH=./lib/javafx-sdk-23.0.1

# Check if the JavaFX SDK folder exists
if [ ! -d "$UNZIPPED_PATH" ]; then
    echo "JavaFX SDK folder not found. Attempting to unzip..."

    # Check if the zip file exists
    if [ ! -f "$ZIP_PATH" ]; then
        echo "Error: JavaFX SDK zip file not found at $ZIP_PATH."
        exit 1
    fi

    # Unzip the file
    unzip "$ZIP_PATH" -d ./lib

    # Check if the unzip was successful
    if [ $? -ne 0 ]; then
        echo "Error: Failed to unzip $ZIP_PATH."
        exit 1
    fi

    echo "JavaFX SDK successfully unzipped to $UNZIPPED_PATH."
else
    echo "JavaFX SDK folder already exists. Skipping unzip."
fi

# Run the project using run.sh
if [ -f "./run.sh" ]; then
    echo "Running the application using run.sh..."
    bash ./run.sh
else
    echo "Error: run.sh file not found in the current directory."
    exit 1
fi
