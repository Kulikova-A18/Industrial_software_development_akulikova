#!/bin/bash

# set -x
# set -e

CLASS_FILES=$(find . -name "*.class" -type f)

if [ -n "$CLASS_FILES" ]; then
    echo "$CLASS_FILES" | sed 's/^/  - /'
    
    rm -f *.class
    
    find . -name "*.class" -type f -delete
fi

javac SatelliteState.java EnergySystem.java SatelliteInfoProvider.java MissionPerformer.java \
       Satellite.java CommunicationSatellite.java ImagingSatellite.java \
       SatelliteConstellation.java Main.java

java Main