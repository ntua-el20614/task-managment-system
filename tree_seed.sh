#!/bin/bash

# =============================================================================
# Script: generate_file_tree.sh
# Description:
#   - Runs 'tree -I "javafx-sdk-23.0.1"' to display the directory structure
#     while excluding 'javafx-sdk-23.0.1'.
#   - Runs 'ls -al' to list all files and directories in long format.
#   - Logs both the commands and their outputs to 'file_tree.txt'.
# Usage:
#   1. Make the script executable:
#        chmod +x generate_file_tree.sh
#   2. Run the script:
#        ./generate_file_tree.sh
# =============================================================================

# Define the output file name
OUTPUT_FILE="file_tree.txt"

# Remove the output file if it already exists to avoid appending to old data
if [ -f "$OUTPUT_FILE" ]; then
    rm "$OUTPUT_FILE"
fi

# Add the introductory text
echo "so, i have this project:" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"  # Add an empty line for readability

# Function to execute a command and log its execution and output
execute_and_log() {
    local cmd="$1"
    echo "Command: $cmd" >> "$OUTPUT_FILE"
    echo "Output:" >> "$OUTPUT_FILE"
    eval "$cmd" >> "$OUTPUT_FILE" 2>&1
    echo "" >> "$OUTPUT_FILE"  # Add an empty line for readability
}

# Check if 'tree' command is available
if ! command -v tree &> /dev/null
then
    echo "'tree' command could not be found. Please install it to proceed."
    echo "You can install 'tree' using Homebrew:"
    echo "    brew install tree"
    exit 1
fi

# Execute the 'tree' command excluding 'javafx-sdk-23.0.1' and log it
execute_and_log 'tree'

# Execute the 'ls -al' command and log it
execute_and_log 'ls -al'

# Add the closing text
cat <<EOF >> "$OUTPUT_FILE"

this is the project:

Application description
In the context of the project a Task Management System will be implemented. The application will allow the user to create, edit, and monitor the available tasks. In addition, the user will be able to manage multiple tasks, set priorities and deadlines, and receive reminders for upcoming tasks.
A.1 Design and implementation logic (40%) The following describes the capabilities that the application should provide to the user regarding the creation and management of tasks and reminders.
Adding, modifying, and deleting tasks. The user will be able to create new tasks. The relevant information for a
task should include: title, description, category, priority,
completion deadline, and status. For simplicity, the completion deadline for a task will not include a time, but will be set at the day level (e.g. 12/14/24). The status of a task will always be one of the following: "Open", "In Progress", "Postponed", "Completed", and "Delayed". For each new task the default status will be "Open". When initializing the application, tasks that are not "Completed" and have passed the
completion deadline and automatically change the status to 'Delayed'.
The user will be able to modify all the elements of a task as well as proceed to delete tasks. In the case of deletions, care must be taken to ensure that the possible reminders set for the task to be deleted are properly updated. Adding, modifying, and deleting a category. The user will be able to define new categories by giving the relevant name. In addition, he/she will be able to modify the name of a category. For simplicity we assume that there are no subcategories. Also, he will be able to delete a category together with the automatic deletion of all tasks belonging to it. In this case the reminders for the deleted tasks should be updated appropriately. Add, modify, and delete priority. The application should include a default priority level named "Default". The user will be able to define new priority levels by giving the relevant name. In addition, it will be possible to modify the name of a priority level as well as delete priority levels. The change name and delete functions will apply to all priority levels except the default. Also, when a priority level is deleted then all tasks belonging to the relevant level shall automatically be assigned the default priority level.

Additional functions
Setting and managing reminders.
The user can create reminders for tasks. A reminder will always be associated with a task, and multiple reminders can be set for a task. If a task has a status of "Completed" there will be no possibility to set reminders, and when the user changes the status of a task to "Completed" then the application will automatically delete any reminders related to that task. 
The application should support the following types of reminders: (i) one day before the deadline, (ii) one week before the deadline, (iii) one month before the deadline, (iv) a specific date defined by the user. Appropriate checks should be implemented to ensure that the selected reminder type is meaningful based on the deadline for completion of the task and that in case of an issue the user is informed with the corresponding message. Finally, the user will be able to modify and delete reminders.
Task Search. The user will be able to search for tasks based on any combinations of the following criteria: title, category, and priority.

A.2. Storage and retrieval of application information (10%)
A solution based on the use of files containing data in JSON format will be used to store and retrieve application information. JSON (JavaScript Object Notation) is a data representation format widely used for storing and transferring data. It is easy to read by humans and, at the same time, understandable by several programming languages. JSON follows a simple structure using text to represent data in a key-value format. More information can be found in the workshop slides in the "Java Networking & JSON" section.

Initially you will need to decide and define your own organization (data schema) for the JSON data and the set of files that will be used to store the application data. The application data files should be stored in a folder named "medialab". Next, you must implement through the appropriate classes the methods that will allow you to retrieve the information in the files and initialize the appropriate objects in your application as well as refresh the files so that the overall state can be maintained between intermittent executions of the application.
We then describe the logic to be implemented to retrieve and update the application data.
- Application initialization: You should be retrieving all the information in the JSON files and at the same time initializing the corresponding objects in your application 
- Application execution: The application will use the state information retrieved in program memory during initialization. All operations related to tasks and reminders managed by the application will be executed based on the information present in the program memory. 
- Application termination: Updating the JSON files with the information
system state information will be done exclusively before application termination. The implementation shall store in the corresponding JSON files the total state of the application at the time of termination.

A.3. Creation of a graphical interface (30%)
You must design and implement the appropriate Graphical User Interface (GUI) using the JavaFX framework [1][2].
Note: The following are the basic specifications for the graphical user interface (GUI).
interface, for all the details of the final implementation you can make any choices you want regarding the appearance and general user interaction with the application, without any impact on the final score. For example, you can choose a simple visualization for the various elements or combine various features from JavaFX to create an effect that corresponds to a modern application. In any case, there is no need to make this part of the task complicated.
Initially when starting the application if there are tasks that are in "Delayed" state the user should be informed with an appropriate popup window about the number of overdue tasks.

About the creation of the GUI you should follow the following instructions
the following general instructions:
- Create the main "window" of the application entitled "MediaLab Assistant" and set the appropriate dimensions.
- Divide the window into two main parts.
- The upper part of the screen will display aggregated information that should be updated accordingly based on the user's actions. The information includes (i) total number of tasks regardless of status, (ii) number of tasks with "Completed" status, (iii) number of tasks with "Delayed" status, and (iv) number of tasks with a completion deadline within 7 days.
- In the other part of the screen, the various functions that the GUI must support should be implemented. There is complete freedom as to how to implement these functions, however, you should ensure that the GUI information is updated appropriately according to the user's actions. The GUI should support the following functions:
- Task management: the application should present the available tasks by category. It should also be possible for the user to define a new task, modify a task and delete a task. The implementation must be in accordance with the corresponding logic from section A.1.
- Category management: the application should present the list of categories. Furthermore, the user should be able to define new categories, change the name of a category and delete categories. The implementation should be done according to the corresponding logic from section A.1.
- Management of priority levels: the application should present the available priority levels. The user should be able to modify the name of priority levels and delete priority levels. The implementation should be done according to the corresponding logic from section A.1.
- Reminder management: the application should present all active reminders. The user should be able to modify a reminder as well as delete reminders. The implementation should be in accordance with the corresponding logic from section A.1.
- Search for tasks: the corresponding form should be available to allow searching for tasks according to the specifications from section A.1. The results should include: title, priority level, category and deadline for completion. 
Finally, your GUI implementation should ensure that when the application is terminated, the system information in the relevant files is updated according to the procedure described in section A.2. A.4. Other requirements (20%)
- The implementation should follow the object-oriented programming (OOP) design principles.
- In a class of your choice, each public method it contains must be documented according to the javadoc tool specification [3]. Note: For anything that is not clear from the pronunciation you can make your own assumptions and assumptions. The pronunciation outlines the basic requirements for the application, however you can make your own design assumptions in trying to make the application more realistic, without making
complex implementation.

i want all views to be fxml files, so that i can edit them accordingly through scene builder

i will tell you on the next message what i want you to do. dont say anything, just analyze
EOF

# Notify the user that the operations are complete
echo "Commands and their outputs have been saved to '$OUTPUT_FILE'."
