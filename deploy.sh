#!/bin/sh
set -e

version=$1
targetHost=$2
username=$3
deployDir=$4
deviceNamePattern=$5

# Stop service
ssh -l ${username} ${targetHost} "sudo service dsound-server stop"

# Initalizations
ssh -l ${username} ${targetHost} "mkdir -p ${deployDir}"
ssh -l ${username} ${targetHost} "sudo usermod -a -G audio pi"

# Copy new files to target host
scp target/dsound-server-${version}.jar ${username}@${targetHost}:${deployDir}/dsound-server.jar
scp dsound-server.conf ${username}@${targetHost}:${deployDir}/

# Install upstart service file and start the service
ssh -l ${username} ${targetHost} "\
  sudo sed -i 's/DEVICE_NAME_PATTERN/${deviceNamePattern}/g' ${deployDir}/dsound-server.conf; \
  sudo cp ${deployDir}/dsound-server.conf /etc/init; \
  sudo service dsound-server start \
  "