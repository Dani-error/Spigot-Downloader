#!/bin/bash

JAVA="java"
JAR="server.jar"
RAM="1000M"
FLAGS="-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC
-XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40
-XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5
-XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15
-XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5
-XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1
-Daikars.new.flags=true -Dusing.aikars.flags=https://mcflags.emc.gs"

while [ true ]; do
    echo "Starting server..."
    ${JAVA} -Xmx${RAM} -Xms${RAM} ${FLAGS} -jar ${JAR} nogui
    for i in 3 2 1; do
        printf 'Server restarting in %s... (press CTRL-C to exit)\n' "${i}"
        sleep 1
    done
done
