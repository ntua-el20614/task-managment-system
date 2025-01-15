import os
import sys

def find_files_with_word(directory, word):
    matched_files = []
    for dirpath, _, filenames in os.walk(directory):
        for filename in filenames:
            filepath = os.path.join(dirpath, filename)
            try:
                # Open the file in text mode
                with open(filepath, 'r', errors='ignore') as file:
                    # Read file content
                    content = file.read()
                    # Check for the exact case-sensitive word
                    if word in content:
                        matched_files.append(filepath)
            except Exception as e:
                # Skip files that can't be read (e.g., binary files)
                continue
    return matched_files

if __name__ == "__main__":
    # Set directory to search; default to current directory if not provided
    directory = sys.argv[1] if len(sys.argv) > 1 else '.'
    word_to_find = "isOverdue"

    files_found = find_files_with_word(directory, word_to_find)

    if files_found:
        print(f"Files containing the word '{word_to_find}':")
        for file in files_found:
            print(file)
    else:
        print(f"No files containing the word '{word_to_find}' were found in {directory}.")
