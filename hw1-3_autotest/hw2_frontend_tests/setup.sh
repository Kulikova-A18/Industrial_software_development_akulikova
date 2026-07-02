#!/bin/bash

set -x

chmod +x gradlew

if command -v dos2unix &> /dev/null; then
    find . -name "*.sh" -exec dos2unix {} \;
    find . -name "*.gradle" -exec dos2unix {} \;
    find . -name "*.gradle.kts" -exec dos2unix {} \;
    find . -name "*.properties" -exec dos2unix {} \;
fi