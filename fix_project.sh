#!/bin/bash

# =============================================================================
# fix_project.sh
# A script to fix the Maven project directory after cleanup.sh introduced
# some issues.
# =============================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# Function to print messages with colors
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
# 2. Fix CategoryService.java
# =============================================================================

echo_info "Fixing CategoryService.java."

# Backup the corrupted CategoryService.java
if [ -f src/main/java/backend/services/CategoryService.java ]; then
    cp src/main/java/backend/services/CategoryService.java src/main/java/backend/services/CategoryService.java.bak
    echo_info "Backup of CategoryService.java created at src/main/java/backend/services/CategoryService.java.bak."
else
    echo_warning "CategoryService.java does not exist at src/main/java/backend/services/CategoryService.java."
fi

# Remove the corrupted CategoryService.java
rm -f src/main/java/backend/services/CategoryService.java
echo_info "Removed corrupted CategoryService.java."

# Recreate CategoryService.java with correct content
cat <<EOF > src/main/java/backend/services/CategoryService.java
package backend.services;

import backend.models.Category;
import backend.services.TaskService;
import backend.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing categories.
 */
public class CategoryService {
    private List<Category> categories;

    /**
     * Constructor that initializes the category list by loading from JSON.
     */
    public CategoryService() {
        this.categories = JsonUtils.loadCategories();
    }

    /**
     * Adds a new category.
     *
     * @param category The category to add.
     */
    public void addCategory(Category category) {
        categories.add(category);
        JsonUtils.saveCategories(categories);
    }

    /**
     * Deletes a category by name.
     *
     * @param name         The name of the category to delete.
     * @param taskService  The TaskService instance to update tasks accordingly.
     */
    public void deleteCategory(String name, TaskService taskService) {
        categories.removeIf(category -> category.getName().equalsIgnoreCase(name));
        JsonUtils.saveCategories(categories);
        // Additional logic to update or delete tasks associated with this category can be added here
    }

    /**
     * Retrieves all categories.
     *
     * @return A list of all categories.
     */
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    // Additional methods as needed
}
EOF

echo_info "CategoryService.java has been recreated with the correct content."

# =============================================================================
# 3. Remove build-helper-maven-plugin from pom.xml
# =============================================================================

echo_info "Removing build-helper-maven-plugin from pom.xml."

# Backup current pom.xml
cp pom.xml pom.xml.backup
echo_info "Backup of pom.xml created at pom.xml.backup."

# Remove build-helper-maven-plugin using sed
# This command deletes from <plugin> with groupId org.codehaus.mojo to </plugin>
sed -i.bak '/<plugin>\s*<groupId>org.codehaus.mojo<\/groupId>/,/<\/plugin>/d' pom.xml

echo_info "build-helper-maven-plugin has been removed from pom.xml."

# =============================================================================
# 4. Move Test Classes to Standard Test Directory
# =============================================================================

echo_info "Moving test classes to src/test/java/backend/services/."

# Create the standard Maven test directory if it doesn't exist
mkdir -p src/test/java/backend/services/

# Move all test classes from backend/tests/services to the new test directory
find backend/tests/services -type f -name '*Test.java' -exec mv {} src/test/java/backend/services/ \; || echo_warning "No test classes to move."

echo_info "Test classes have been moved to src/test/java/backend/services/."

# =============================================================================
# 5. Remove Any Remaining Empty Test Directories
# =============================================================================

echo_info "Removing old test directories if empty."

find backend/tests -type d -empty -delete || echo_warning "No empty test directories to remove."

echo_info "Old test directories have been cleaned up."

# =============================================================================
# 6. Ensure No Test Code Exists in Main Service Classes
# =============================================================================

echo_info "Ensuring that no test code exists in main service classes."

# Iterate over all main service Java files
MAIN_SERVICE_FILES=$(find src/main/java/backend/services -type f -name '*.java')

for file in $MAIN_SERVICE_FILES; do
    echo_info "Checking $file for test code."

    # Remove JUnit imports
    if grep -q "import org.junit.jupiter.api" "$file"; then
        echo_warning "Removing JUnit imports from $file."
        sed -i.bak '/import org.junit.jupiter.api/d' "$file"
    fi

    # Remove @Test annotations
    if grep -q "@Test" "$file"; then
        echo_warning "Removing @Test annotations from $file."
        sed -i.bak '/@Test/d' "$file"
    fi

    # Remove @BeforeEach annotations
    if grep -q "@BeforeEach" "$file"; then
        echo_warning "Removing @BeforeEach annotations from $file."
        sed -i.bak '/@BeforeEach/d' "$file"
    fi

    # Remove any stray test methods containing 'assert' (simple heuristic)
    if grep -q "assert" "$file"; then
        echo_warning "Potential test methods found in $file. Please review manually."
    fi
done

echo_info "Main service classes have been cleaned of test code."

# =============================================================================
# 7. Final Clean Build
# =============================================================================

echo_info "Performing a clean build of the project."

mvn clean compile test

echo_info "Project has been cleaned, fixed, and built successfully!"

