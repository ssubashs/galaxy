#!/usr/bin/env bash

set -e

# export docker-machine IP
IP=127.0.0.1
export EXTERNAL_IP=$IP

# Get this script directory (to find yml from any directory)
export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Stop
docker-compose -f $DIR/docker-compose.yml stop

# Start container cluster
# First start persistence and auth container and wait for it
#docker-compose -f $DIR/docker-compose.yml up -d elasticsearch logstash kibana mysql mongo redis keycloak-server
echo "Waiting for Cleaning old process..."
sleep 15

# Start other containers
docker-compose -f $DIR/docker-compose.yml up