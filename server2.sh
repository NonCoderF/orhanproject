#!/bin/sh
echo "Starting Orhan Server..."
echo "Host : wsc -er ws://0.0.0.0:8080/socket?userId=dev"
echo "Protocol : {\"receivers":["dev"]}\"

echo "Locating project..."
cd ~/AndroidProject/DS/orhanproject || exit
./gradlew run
