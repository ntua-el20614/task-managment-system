#!/usr/bin/env bash

# run_windows.sh - Compiles and runs the Task Management System JavaFX application with Gluon Glisten on Windows

# Exit immediately if a command exits with a non-zero status
set -e

# Function to print error messages and exit
function error_exit {
    echo "$1" >&2
    exit 1
}

# Set the paths to other libraries
PATH_TO_GLISTEN_LIB="./lib/charm-glisten-5.0.0.jar"
PATH_TO_GSON_LIB="./lib/gson-2.10.1.jar"
PATH_TO_BCRYPT_LIB="./lib/jbcrypt-0.4.jar"

# Set the path to JavaFX SDK using a wildcard to include all JARs
# Convert the list of JARs into a semicolon-separated string for Windows
PATH_TO_FX=$(echo ./lib/javafx-sdk-23.0.2/lib/*.jar | tr ' ' ';')

# Replace backslashes with forward slashes in PATH_TO_FX (if any)
PATH_TO_FX=${PATH_TO_FX//\\//}

# Debugging: Print the module path
echo "Module Path: $PATH_TO_FX"

# Check if JavaFX SDK JARs exist
if [ -z "$PATH_TO_FX" ]; then
    error_exit "JavaFX SDK JARs not found at ./lib/javafx-sdk-23.0.2/lib/"
fi

# Check if Gluon Glisten JAR exists
if [ ! -f "$PATH_TO_GLISTEN_LIB" ]; then
    error_exit "Gluon Glisten JAR not found at $PATH_TO_GLISTEN_LIB"
fi

# Check if Gson JAR exists
if [ ! -f "$PATH_TO_GSON_LIB" ]; then
    error_exit "Gson JAR not found at $PATH_TO_GSON_LIB"
fi

# Check if BCrypt JAR exists
if [ ! -f "$PATH_TO_BCRYPT_LIB" ]; then
    error_exit "BCrypt JAR not found at $PATH_TO_BCRYPT_LIB"
fi

# Create output directory if it doesn't exist
mkdir -p out

# Clean the output directory to prevent residual files
rm -rf out/*

# Compile the application and controllers
echo "Compiling..."
javac \
    --module-path "$PATH_TO_FX" \
    --add-modules javafx.controls,javafx.fxml \
    -cp "$PATH_TO_GLISTEN_LIB;$PATH_TO_GSON_LIB;$PATH_TO_BCRYPT_LIB" \
    -d out \
    src/App.java \
    src/Controllers/AddTaskDialog.java \
    src/Controllers/AddTaskDialogController.java \
    src/Controllers/AuthController.java \
    src/Controllers/EditTaskDialog.java \
    src/Controllers/EditTaskDialogController.java \
    src/Controllers/LoginViewController.java \
    src/Controllers/MainControllerWrapper.java \
    src/Controllers/MainViewControllerDetailed.java \
    src/Controllers/MainViewListController.java \
    src/Controllers/ReminderAlertController.java \
    src/Controllers/RemindersViewerController.java \
    src/Controllers/AddEditReminderDialogController.java \
    src/Controllers/SettingsViewController.java \
    src/Models/Task.java \
    src/Models/User.java \
    src/Utils/JsonUtils.java \
    src/Utils/PasswordUtils.java

echo "Compilation successful."

# Copy Views and Media directories to out for resource access
cp -R src/Views out/Views
cp -R src/Media out/Media

# Verify that resources were copied successfully
if [ ! -d "out/Views" ]; then
    error_exit "Failed to copy Views resources."
fi

if [ ! -d "out/Media" ]; then
    error_exit "Failed to copy Media resources."
fi

# Determine the fully qualified main class name
# Since your App class is in the 'src' package, set MAIN_CLASS to 'src.App'
MAIN_CLASS="src.App"

# Verify that the main class exists
if [[ "$MAIN_CLASS" == *"."* ]]; then
    # Convert package name to path
    MAIN_CLASS_PATH=$(echo "out/${MAIN_CLASS//.//}.class")
else
    MAIN_CLASS_PATH="out/${MAIN_CLASS}.class"
fi

if [ ! -f "$MAIN_CLASS_PATH" ]; then
    error_exit "Main class $MAIN_CLASS not found. Please ensure the MAIN_CLASS variable is set correctly."
fi

# Run the application
echo "Running the application..."
java \
    --module-path "$PATH_TO_FX" \
    --add-modules javafx.controls,javafx.fxml \
    -cp "out;$PATH_TO_GLISTEN_LIB;$PATH_TO_GSON_LIB;$PATH_TO_BCRYPT_LIB" \
    "$MAIN_CLASS"
