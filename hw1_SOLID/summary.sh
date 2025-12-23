#!/bin/bash

# set -x
# set -e

# ==================================================================
# Shows statistics and information about the build and run
# ==================================================================

SOURCE_FILE="ZooManagementSystemDemo.java"

echo "Execution Summary:"
echo "  Start time:   $(date)"
echo "  Directory:    $(pwd)"
echo "  Source file:  $SOURCE_FILE"

# Count source lines
if [ -f "$SOURCE_FILE" ]; then
    LINE_COUNT=$(wc -l < "$SOURCE_FILE")
    echo "  Source lines: $LINE_COUNT"
fi

# Calculate total size of class files
CLASS_FILES=$(find . -name "*.class" -type f)
if [ -n "$CLASS_FILES" ]; then
    TOTAL_SIZE=0
    CLASS_COUNT=0
    
    for file in $CLASS_FILES; do
        if [ -f "$file" ]; then
            size=$(stat -c%s "$file" 2>/dev/null || stat -f%z "$file" 2>/dev/null)
            TOTAL_SIZE=$((TOTAL_SIZE + size))
            CLASS_COUNT=$((CLASS_COUNT + 1))
        fi
    done
    
    if [ $CLASS_COUNT -gt 0 ]; then
        echo "  Class files:  $CLASS_COUNT file(s)"
        
        # Format size for display
        if [ $TOTAL_SIZE -lt 1024 ]; then
            SIZE_DISPLAY="${TOTAL_SIZE} bytes"
        elif [ $TOTAL_SIZE -lt 1048576 ]; then
            SIZE_DISPLAY="$((TOTAL_SIZE / 1024)) KB"
        else
            SIZE_DISPLAY="$((TOTAL_SIZE / 1048576)) MB"
        fi
        echo "  Total size:   $SIZE_DISPLAY"
    fi
fi

echo ""