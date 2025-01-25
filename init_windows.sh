#!/usr/bin/env bash

# init_windows.sh - Unzips JavaFX SDK for Windows if needed and runs the Task Management System

# Exit immediately if a command exits with a non-zero status
set -e

# Function to print error messages and exit
function error_exit {
    echo "$1" >&2
    exit 1
}

# Path to JavaFX SDK zip
ZIP_PATH=./lib/javafx-sdk-23.0.2.zip

# Path to target unzipped folder
UNZIPPED_PATH=./lib/javafx-sdk-23.0.2

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

# Update run_windows.sh with the new JavaFX SDK path if needed
# (Assuming run_windows.sh references the correct path)

# Run the project using run_windows.sh
if [ -f "./run_windows.sh" ]; then
    echo "Running the application using run_windows.sh..."
    bash ./run_windows.sh
else
    echo "Error: run_windows.sh file not found in the current directory."
    exit 1
fi
