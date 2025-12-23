#!/bin/bash

# set -x
# set -e

# ==================================================================
# Compiles ZooManagementSystemDemo.java with UTF-8 encoding
# ==================================================================

SOURCE_FILE="ZooManagementSystemDemo.java"

echo "Compiling $SOURCE_FILE..."

# Compile with UTF-8 encoding
if javac -encoding UTF-8 "$SOURCE_FILE"; then
    echo "Compilation completed successfully"
    
    # List created class files
    echo "Generated .class files:"
    CLASS_COUNT=0
    for class_file in *.class; do
        if [ -f "$class_file" ]; then
            size=$(stat -c%s "$class_file" 2>/dev/null || stat -f%z "$class_file" 2>/dev/null)
            echo "  - $class_file ($size bytes)"
            CLASS_COUNT=$((CLASS_COUNT + 1))
        fi
    done
    
    if [ $CLASS_COUNT -eq 0 ]; then
        echo "  (No .class files found in current directory)"
    else
        echo "  Total: $CLASS_COUNT file(s)"
    fi
else
    echo "Compilation failed"
    exit 1
fi

echo ""