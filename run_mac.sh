#!/bin/bash

# run.sh - Compiles and runs the Task Management System JavaFX application with Gluon Glisten

# Set the path to JavaFX SDK
PATH_TO_FX=./lib/javafx-sdk-23.0.1/lib

# Set the path to Gluon Glisten library
PATH_TO_GLISTEN_LIB=./lib/charm-glisten-5.0.0.jar

# Set the path to Gson library
PATH_TO_GSON_LIB=./lib/gson-2.10.1.jar

# Set the path to BCrypt library
PATH_TO_BCRYPT_LIB=./lib/jbcrypt-0.4.jar

# Check if PATH_TO_FX exists
if [ ! -d "$PATH_TO_FX" ]; then
    echo "JavaFX SDK not found at $PATH_TO_FX"
    exit 1
fi

# Check if Gluon Glisten JAR exists
if [ ! -f "$PATH_TO_GLISTEN_LIB" ]; then
    echo "Gluon Glisten JAR not found at $PATH_TO_GLISTEN_LIB"
    exit 1
fi

# Check if Gson JAR exists
if [ ! -f "$PATH_TO_GSON_LIB" ]; then
    echo "Gson JAR not found at $PATH_TO_GSON_LIB"
    exit 1
fi

# Check if BCrypt JAR exists
if [ ! -f "$PATH_TO_BCRYPT_LIB" ]; then
    echo "BCrypt JAR not found at $PATH_TO_BCRYPT_LIB"
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p out

# Clean the output directory to prevent residual files
rm -rf out/*

# Compile the application and controllers
javac --module-path "$PATH_TO_FX:$PATH_TO_GLISTEN_LIB" \
      --add-modules javafx.controls,javafx.fxml,charm.glisten \
      -cp "$PATH_TO_GSON_LIB:$PATH_TO_BCRYPT_LIB" \
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

# Check if compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

echo "Compilation successful."

# Copy Views directory to out for resource access
cp -R src/Views out/

# Copy Media directory to out for resource access
cp -R src/Media out/

# Run the application
echo "Running the application..."
java --module-path "$PATH_TO_FX:$PATH_TO_GLISTEN_LIB" \
     --add-modules javafx.controls,javafx.fxml,charm.glisten \
     -cp "out:$PATH_TO_GSON_LIB:$PATH_TO_BCRYPT_LIB" \
     src/App.java
