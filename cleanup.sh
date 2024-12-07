#!/bin/bash

# =============================================================================
# cleanup.sh
# A script to clean and reorganize the Maven project directory for the
# Task Management System.
# =============================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# Function to print messages
function echo_info {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

function echo_warning {
    echo -e "\033[1;33m[WARNING]\033[0m $1"
}

function echo_error {
    echo -e "\033[1;31m[ERROR]\033[0m $1"
}

# =============================================================================
# 1. Navigate to Project Root
# =============================================================================

echo_info "Navigating to project root directory."
cd /Users/chris/Desktop/task-managment-system

# =============================================================================
# 2. Remove Build Output Directories and Stray Files
# =============================================================================

echo_info "Removing build output directories and stray files."
rm -rf out target
rm -f task.json

# =============================================================================
# 3. Remove Duplicate Test Classes from Main Source Directories
# =============================================================================

echo_info "Removing duplicate TaskServiceTest.java files from main source directories."

# Find all TaskServiceTest.java files
TEST_FILES=$(find backend -type f -name 'TaskServiceTest.java')

# Define the correct test directory
CORRECT_TEST_DIR="./backend/tests/services/TaskServiceTest.java"

for file in $TEST_FILES; do
    if [[ "$file" != "$CORRECT_TEST_DIR" ]]; then
        echo_info "Removing duplicate test file: $file"
        rm -f "$file"
    else
        echo_info "Keeping test file: $file"
    fi
done

# =============================================================================
# 4. Clean Up JavaFX Dependencies in Maven Local Repository
# =============================================================================

echo_info "Removing conflicting JavaFX dependencies from Maven local repository."

# Define JavaFX version compatible with Java 17
JAVA_FX_VERSION="17.0.2"

# Remove older JavaFX versions (e.g., 23.0.1)
rm -rf ~/.m2/repository/org/openjfx/javafx-graphics/23.0.1
rm -rf ~/.m2/repository/org/openjfx/javafx-controls/23.0.1

# =============================================================================
# 5. Remove Compiled Class Files
# =============================================================================

echo_info "Removing stray .class files from backend and frontend directories."

find backend -type f -name '*.class' -exec rm -f {} +
find frontend -type f -name '*.class' -exec rm -f {} +

# =============================================================================
# 6. Transition to Maven's Standard Directory Structure
# =============================================================================

echo_info "Creating standard Maven directory structure."

mkdir -p src/main/java/backend/models
mkdir -p src/main/java/backend/services
mkdir -p src/main/java/backend/utils
mkdir -p src/main/java/frontend
mkdir -p src/test/java/backend/services
mkdir -p src/main/resources/data
mkdir -p src/test/resources/data

# =============================================================================
# 7. Move Existing Source Files to Standard Locations
# =============================================================================

echo_info "Moving backend models to src/main/java/backend/models/"
mv backend/models/*.java src/main/java/backend/models/ || echo_warning "No models to move."

echo_info "Moving backend services to src/main/java/backend/services/"
mv backend/services/*.java src/main/java/backend/services/ || echo_warning "No services to move."

echo_info "Moving backend utils to src/main/java/backend/utils/"
mv backend/utils/*.java src/main/java/backend/utils/ || echo_warning "No utils to move."

echo_info "Moving frontend classes to src/main/java/frontend/"
mv frontend/*.java src/main/java/frontend/ || echo_warning "No frontend classes to move."

echo_info "Moving test classes to src/test/java/backend/services/"
mv backend/tests/services/*.java src/test/java/backend/services/ || echo_warning "No test classes to move."

echo_info "Moving resource JSON files to src/main/resources/data/"
mv backend/utils/data/*.json src/main/resources/data/ || echo_warning "No resource files to move."

# =============================================================================
# 8. Remove Old Directories
# =============================================================================

echo_info "Removing old backend and frontend directories."
rm -rf backend/frontend backend/tests services utils frontend

# =============================================================================
# 9. Update pom.xml to Reflect Standard Directory Structure
# =============================================================================

echo_info "Updating pom.xml to remove build-helper-maven-plugin."

# Backup the original pom.xml
cp pom.xml pom.xml.bak

# Use sed to remove the build-helper-maven-plugin section
sed -i.bak '/<plugin>\s*<groupId>org.codehaus.mojo<\/groupId>/,/<\/plugin>/d' pom.xml

echo_info "pom.xml has been updated. A backup is saved as pom.xml.bak."

# =============================================================================
# 10. Final Clean Build
# =============================================================================

echo_info "Performing a clean build of the project."

mvn clean compile test

echo_info "Cleanup and reorganization completed successfully!"

# =============================================================================
# End of cleanup.sh
# =============================================================================

