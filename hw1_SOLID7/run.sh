#!/bin/bash

set -xe

rm -rf out/ build/ gradle/ gradlew gradlew.bat

java -version

curl -L -o gradle.zip https://services.gradle.org/distributions/gradle-8.5-bin.zip
unzip -q gradle.zip
./gradle-8.5/bin/gradle wrapper
rm -rf gradle.zip gradle-8.5
chmod +x gradlew

mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt

if [ $? -eq 0 ]; then
    java -cp out com.example.Main
else
    exit 1
fi

./gradlew clean test

[ -f "build/reports/tests/test/index.html" ] && echo "Отчет о тестах: file://$(pwd)/build/reports/tests/test/index.html"
[ -f "build/reports/jacoco/test/html/index.html" ] && echo "Отчет JaCoCo: file://$(pwd)/build/reports/jacoco/test/html/index.html"