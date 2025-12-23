#!/bin/bash

# ==================================================================
# Verifies that Java and javac are installed
# ==================================================================

echo "Checking dependencies..."

# Check if javac (Java compiler) is installed
if ! command -v javac &> /dev/null; then
    echo "Java compiler (javac) not found"
    echo ""
    echo "To install Java Development Kit:"
    echo "  Ubuntu/Debian: sudo apt install openjdk-11-jdk"
    echo "  CentOS/RHEL:   sudo yum install java-11-openjdk-devel"
    echo "  Fedora:        sudo dnf install java-11-openjdk-devel"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "Java Runtime (java) not found"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2)
JAVAC_VERSION=$(javac -version 2>&1)
echo "Java installed - Version: $JAVA_VERSION"
echo "Java compiler - $JAVAC_VERSION"

SOURCE_FILE="ZooManagementSystemDemo.java"
if [ ! -f "$SOURCE_FILE" ]; then
    echo "Source file $SOURCE_FILE not found in current directory"
    echo "Current directory contents:"
    ls -la
    exit 1
fi

echo "Source file found: $SOURCE_FILE ($(wc -l < "$SOURCE_FILE") lines)"
echo ""