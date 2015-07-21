#!/bin/sh
version=$1
targetHost=$2
username=$3
deployDir=$4

# copy files to target host
ssh -l ${username} ${targetHost} "cd ${deployDir}; [ ! -f dsound-server.jar ] || mv -f dsound-server.jar dsound-server.jar.old"
scp dsound-server/target/dvision-server-${version}.jar ${username}@${targetHost}:${deployDir}/dsound-server.jar

# Restart service
ssh -l ${username} ${targetHost} "sudo service dsound-server restart"

# Initialization on targetHost:
# $ sudo cp dsound-server.conf /etc/init
