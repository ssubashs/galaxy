#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Start mvn build for all modules......"

cd ../

mvn clean package -U

echo "mvn build complete......"

echo "cleaning docker images "

docker-compose -f $DIR/docker-compose.yml down

docker rmi "vertx/accountservice"
docker rmi "vertx/gateway"
docker rmi "vertx/metadataservice"


echo "Starting docker build ......"

docker build -t "vertx/accountservice" $DIR/../accountservice

#docker build -t "vertx/clustermanager" $DIR/../clustermanager

docker build -t "vertx/gateway" $DIR/../gateway

docker build -t "vertx/metadataservice" $DIR/../metadataservice

echo "docker build complete ......"

docker images