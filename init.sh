#!/bin/bash

# init.sh - Initializes the project folder structure for the MediaLab Assistant application

# Function to create directories
create_directories() {
    echo "Creating src directory structure..."

    mkdir -p src/Controllers
    mkdir -p src/Views
    mkdir -p src/Models

    echo "Directories created successfully:"
    tree src
}

# Check if src directory already exists
if [ -d "src" ]; then
    echo "The 'src' directory already exists."
    echo "Do you want to overwrite the existing Controllers, Views, and Models directories? (y/n)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        rm -rf src/Controllers src/Views src/Models
        create_directories
    else
        echo "Initialization aborted. Existing directory structure preserved."
        exit 1
    fi
else
    create_directories
fi

echo "Initialization complete."
