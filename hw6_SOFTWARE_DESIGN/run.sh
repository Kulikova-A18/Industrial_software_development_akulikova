#!/bin/bash

set -ex

if ! command -v javac &> /dev/null || ! command -v java &> /dev/null; then
    echo "Ошибка: Java не установлена. Установите JDK."
    exit 1
fi

find . -name "*.class" -delete 2>/dev/null

javac -d . \
    com/tigerbank/enums/OperationType.java \
    com/tigerbank/domain/BankAccount.java \
    com/tigerbank/domain/Category.java \
    com/tigerbank/domain/Operation.java \
    com/tigerbank/repository/Repository.java \
    com/tigerbank/repository/BankAccountRepository.java \
    com/tigerbank/repository/CategoryRepository.java \
    com/tigerbank/repository/OperationRepository.java \
    com/tigerbank/service/AccountService.java \
    com/tigerbank/service/CategoryService.java \
    com/tigerbank/service/OperationService.java \
    com/tigerbank/service/AnalyticsService.java \
    com/tigerbank/service/FileExporter.java \
    com/tigerbank/gui/utils/UIHelper.java \
    com/tigerbank/gui/dialogs/AccountDialog.java \
    com/tigerbank/gui/dialogs/CategoryDialog.java \
    com/tigerbank/gui/dialogs/OperationDialog.java \
    com/tigerbank/gui/components/AccountsPanel.java \
    com/tigerbank/gui/components/CategoriesPanel.java \
    com/tigerbank/gui/components/OperationsPanel.java \
    com/tigerbank/gui/components/AnalyticsPanel.java \
    com/tigerbank/gui/MainFrame.java \
    com/tigerbank/Main.java

if [ $? -eq 0 ]; then
    java com.tigerbank.Main
else
    exit 1
fi