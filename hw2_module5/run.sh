#!/bin/bash

if ! command -v javac &> /dev/null; then
    echo "[Error] Java Compiler (javac) is not installed or not in PATH"
    echo "Please install Java Development Kit (JDK) first"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "[Error] Java Runtime (java) is not installed or not in PATH"
    echo "Please install Java Runtime Environment (JRE) or JDK"
    exit 1
fi

echo "Java version:"
java -version
echo ""

find . -name "*.class" -type f -delete

if [ ! -f "BankApplication.java" ]; then
    echo "[Error] BankApplication.java not found in current directory"
    ls -la *.java
    exit 1
fi

java_files_count=$(ls *.java 2>/dev/null | wc -l)
echo "Found $java_files_count Java file(s) in current directory"

javac BankApplication.java

if [ $? -eq 0 ]; then
    ls -la *.class 2>/dev/null || echo "No class files found (unexpected)"
    
    echo ""
    echo "Starting the program..."
    
    java BankApplication
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "[Warning] Program terminated with non-zero exit code"
    fi
    
else
    echo ""
    echo "[Error] Compilation failed!"
    echo "Please check your Java code for errors."
    exit 1
fi
