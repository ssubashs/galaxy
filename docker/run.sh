#!/usr/bin/env bash

set -e

# export docker-machine IP
IP=127.0.0.1
export EXTERNAL_IP=$IP

# Get this script directory (to find yml from any directory)
export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Stop
docker-compose -f $DIR/docker-compose.yml stop
#docker-compose -f $DIR/elk-docker-compose.yml stop

# Start container cluster

#docker-compose -f $DIR/docker-compose.yml up -d elasticsearch logstash kibana account-service metadata-service api-gateway
echo "Waiting for Cleaning old process..."
sleep 10

# Start other containers
docker-compose -f $DIR/docker-compose.yml up
#docker-compose -f $DIR/docker-compose.yml up