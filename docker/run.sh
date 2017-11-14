#!/usr/bin/env bash

set -e

# export docker-machine IP
IP=127.0.0.1
export EXTERNAL_IP=$IP

# Get this script directory (to find yml from any directory)
export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


# Stop
echo " stop the existing services/pods/deployments"
kompose -f $DIR/docker-compose.yml down

sleep 10

echo "installing deployments/pods/services"

kubectl create -f  $DIR/kubernetes-v2/account-service-deployment.yaml
kubectl create -f  $DIR/kubernetes-v2/account-service-service.yaml
kubectl create -f  $DIR/kubernetes-v2/metadata-service-deployment.yaml
kubectl create -f  $DIR/kubernetes-v2/metadata-service-service.yaml
#kubectl create -f  $DIR/api-gateway-deployment.yaml
#kubectl create -f  $DIR/api-gateway-service.yaml

#kubectl create -f  $DIR/apigate-ingress.yaml

sleep 10

echo "deployed all the services"
