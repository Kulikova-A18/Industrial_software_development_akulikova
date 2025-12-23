#!/bin/bash

# set -x
# set -e

# ==================================================================
# Removes all .class files from the current directory
# ==================================================================

echo "Cleaning old class files..."

CLASS_FILES=$(find . -name "*.class" -type f)

if [ -n "$CLASS_FILES" ]; then
    echo "Found .class files to remove:"
    echo "$CLASS_FILES" | sed 's/^/  - /'
    
    rm -f *.class
    
    # Also remove any .class files in subdirectories
    find . -name "*.class" -type f -delete
    
    echo "All .class files removed"
else
    echo "No .class files found, skipping cleanup"
fi

echo ""