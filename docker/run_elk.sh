#!/usr/bin/env bash

set -e

# export docker-machine IP
IP=127.0.0.1
export EXTERNAL_IP=$IP

# Get this script directory (to find yml from any directory)
export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

kubectl create -f  $DIR/elasticsearch-deployment.yaml
kubectl create -f  $DIR/elasticsearch-service.yaml
kubectl create -f  $DIR/logstash-deployment.yaml
kubectl create -f  $DIR/logstash-service.yaml
kubectl create -f  $DIR/kibana-deployment.yaml
kubectl create -f  $DIR/kibana-service.yaml

sleep 10
echo "deployed all services for elk stack"