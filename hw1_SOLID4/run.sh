#!/bin/bash

set -xe

rm -rf out/
rm -rf build/

rm -rf gradle/
rm -rf gradlew
rm -rf gradlew.bat

gradle wrapper --gradle-version 8.5

chmod +x gradlew

mkdir -p out

javac -d out src/main/*.java

if [ $? -eq 0 ]; then
    java -cp out com.example.Main
else
    exit 1
fi

if [ ! -f "./gradlew" ]; then
    gradle wrapper
fi

chmod +x gradlew

./gradlew clean test

if [ -f "build/reports/tests/test/index.html" ]; then
    echo ""
    echo "Отчет о тестах: file://$(pwd)/build/reports/tests/test/index.html"
fi

if [ -f "build/reports/jacoco/test/html/index.html" ]; then
    echo "Отчет JaCoCo о покрытии: file://$(pwd)/build/reports/jacoco/test/html/index.html"
fi