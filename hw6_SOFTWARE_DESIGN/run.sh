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
find . -name "*.json" -type f -delete
find . -name "*.csv" -type f -delete


javac *.java
java Main
