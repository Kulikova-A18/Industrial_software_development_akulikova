#!/bin/bash

# set -x
# set -e

# ==================================================================
# Executes ZooManagementSystemDemo with proper error handling
# ==================================================================

echo "------------------------------------------------"
echo "         Zoo Management System Output"
echo "------------------------------------------------"

# Run
java ZooManagementSystemDemo
EXIT_CODE=$?

echo "------------------------------------------------"

if [ $EXIT_CODE -eq 0 ]; then
    echo "Program completed successfully (exit code: $EXIT_CODE)"
else
    echo "Program failed with exit code: $EXIT_CODE"
    exit $EXIT_CODE
fi

echo ""