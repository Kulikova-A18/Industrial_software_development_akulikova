#!/bin/bash

set -e

echo "========================================="
echo "  СБОРКА И ЗАПУСК MISSION SCHEDULER"
echo "========================================="
echo ""

if ! command -v mvn &> /dev/null; then
    echo "Maven не найден. Установка Maven..."
    sudo apt install maven -y
fi

echo "Java version:"
java -version
echo ""

echo "Очистка предыдущей сборки..."
mvn clean

echo "Сборка проекта..."
mvn compile

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "  ЗАПУСК ПРИЛОЖЕНИЯ"
    echo "========================================="
    echo ""
    
    mvn exec:java -Dexec.mainClass="com.example.Main"
else
    echo "Ошибка сборки!"
    exit 1
fi