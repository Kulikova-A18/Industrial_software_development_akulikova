#!/bin/bash

set -ex

cd ~/Industrial_software_development_akulikova/hw4_sync

echo "=== Running Unit Tests ==="
cd file-storing-service && mvn test -q && cd ..
cd file-analysis-service && mvn test -q && cd ..
cd api-gateway && mvn test -q && cd ..

./start.sh &
sleep 20

./test-api.sh

./integration-test.sh

./stop.sh
