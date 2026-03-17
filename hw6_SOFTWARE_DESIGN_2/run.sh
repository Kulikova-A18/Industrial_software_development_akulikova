#!/bin/bash

set -ex

if ! command -v javac &> /dev/null || ! command -v java &> /dev/null; then
    echo "Ошибка: Java не установлена. Установите JDK."
    exit 1
fi

find . -name "*.class" -delete 2>/dev/null

# Добавляем JSON библиотеку в classpath
JSON_JAR="lib/json-20210307.jar"
JUNIT_JAR="lib/junit-platform-console-standalone-1.8.2.jar"

# Скачиваем JSON библиотеку если её нет
if [ ! -f "$JSON_JAR" ]; then
    echo "Скачиваем JSON библиотеку..."
    mkdir -p lib
    wget -O "$JSON_JAR" https://repo1.maven.org/maven2/org/json/json/20210307/json-20210307.jar
fi

# Компилируем все исходники
echo "Компиляция основного кода..."
javac -cp ".:$JSON_JAR" -d . \
    com/tigerbank/di/*.java \
    com/tigerbank/command/*.java \
    com/tigerbank/enums/*.java \
    com/tigerbank/domain/*.java \
    com/tigerbank/domain/factory/*.java \
    com/tigerbank/repository/*.java \
    com/tigerbank/service/*.java \
    com/tigerbank/facade/*.java \
    com/tigerbank/exporter/*.java \
    com/tigerbank/importer/*.java \
    com/tigerbank/gui/utils/*.java \
    com/tigerbank/gui/dialogs/*.java \
    com/tigerbank/gui/components/*.java \
    com/tigerbank/gui/*.java \
    com/tigerbank/*.java

if [ $? -eq 0 ]; then
    echo "Компиляция успешна. Запуск приложения..."
    java -cp ".:$JSON_JAR" com.tigerbank.Main
else
    echo "Ошибка компиляции!"
    exit 1
fi