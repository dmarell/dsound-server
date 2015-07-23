#!/bin/sh

version=$1
targetHost=$2
username=$3
deployDir=$4
deviceNamePattern=$5

# The deviceNamePattern should match one of the devices reported in the log when starting up:
#
# 2015-07-23 07:11:40,498 [main] INFO  s.m.dsoundserver.PlayController - Existing sound player devices:
# 	ALSA [default]
# 	ALSA [plughw:0,0]
# 	ALSA [plughw:0,1]
# 	U0xccd0x77 [plughw:1,0]
#
# In order the match the last entry in this example (a USB-sound card), pass the value "U0xccd0x77.*"
# as the fifth parameter to this script.
#

# Stop service
ssh -l ${username} ${targetHost} "sudo service dsound-server stop" 2> /dev/null

set -e

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