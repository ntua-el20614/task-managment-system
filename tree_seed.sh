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
execute_and_log 'tree -I "javafx-sdk-23.0.1"'

# Execute the 'ls -al' command and log it
execute_and_log 'ls -al'

# Notify the user that the operations are complete
echo "Commands and their outputs have been saved to '$OUTPUT_FILE'."

