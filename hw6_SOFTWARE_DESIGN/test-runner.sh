#!/bin/bash

CLASSPATH=".:junit-4.13.2.jar:hamcrest-core-1.3.jar"

find . -name "*Test.class" -delete 2>/dev/null

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
    com/tigerbank/service/FileExporter.java 2>&1 | grep -v "Note:"

ERROR=0
for testfile in test/com/tigerbank/domain/*.java test/com/tigerbank/repository/*.java test/com/tigerbank/service/*.java; do
    if [ -f "$testfile" ]; then
        javac -cp "$CLASSPATH" -d . "$testfile" 2>&1
        if [ $? -ne 0 ]; then
            ERROR=1
        fi
    fi
done

if [ $ERROR -eq 0 ]; then
    java -cp "$CLASSPATH" org.junit.runner.JUnitCore \
        com.tigerbank.domain.BankAccountTest \
        com.tigerbank.domain.CategoryTest \
        com.tigerbank.domain.OperationTest 2>&1
    
    java -cp "$CLASSPATH" org.junit.runner.JUnitCore \
        com.tigerbank.repository.BankAccountRepositoryTest \
        com.tigerbank.repository.CategoryRepositoryTest \
        com.tigerbank.repository.OperationRepositoryTest 2>&1
    
    java -cp "$CLASSPATH" org.junit.runner.JUnitCore \
        com.tigerbank.service.AccountServiceTest \
        com.tigerbank.service.CategoryServiceTest \
        com.tigerbank.service.OperationServiceTest 2>&1
else
    for testfile in test/com/tigerbank/domain/*.java; do
        if [ -f "$testfile" ]; then
            javac -cp "$CLASSPATH" -d . "$testfile" 2>&1 | grep -i error
        fi
    done
fi