#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Start mvn build for all modules......"

cd ../

mvn clean package -U

echo "mvn build complete......"

#echo "cleaning docker images "
#
#docker-compose -f $DIR/docker-compose.yml down
#
#docker rmi "localhost:5000/accountservice"
#docker rmi "localhost:5000/gateway"
#docker rmi "localhost:5000/metadataservice"


echo "Starting docker build ......"

docker build -t "localhost:5000/accountservice" $DIR/../accountservice
docker push "localhost:5000/accountservice"

#docker build -t "vertx/clustermanager" $DIR/../clustermanager

#docker build -t "localhost:5000/gateway" $DIR/../gateway
#docker push "localhost:5000/gateway"

docker build -t "localhost:5000/metadataservice" $DIR/../metadataservice
docker push "localhost:5000/metadataservice"

echo "docker build complete ......"

docker images