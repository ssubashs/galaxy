## galaxy
  - vertx microservices
  - deployed using docker-compose(api gateway) or using kubernetes(ingress to route based on route to microservice). 
  - elk stack for logging using lobback.
  - vertx service discovery for eventbus services and rest services
  - circuit breaker for broken rest api calls and broken service call. 
  - referrence implementation of vertx service proxy. 
  - git submodules and mvn module setup for release. 

## pending
  - kubernetes dns discovery(or using kubenetes api's) for vertx clustering. 
  - deploy in gcp. 
  - helm and graph with jenkins pipe-line.
  - management console for vertx rest and event services(sample app using react or angularjs.)
  - elastic search container crashes in minikube. ?? 
  
  
## test

### 
    - sh ./docker/build.sh
    - sh ./docker/run.sh

### docker end points
    
    - [metadata service] http://localhost:8190/account?id=3
    - [metadata service] http://localhost:8190/country?id=3
    - [metadata service] http://localhost:8190/country?id=3  
    - [Account service] http://localhost:8090/info/2
    - [Gateway]  http://localhost:8050/api/metadata/account?id=3
    - [Gateway]  http://localhost:8050/api/metadata/country?id=3
    - [Gateway]  http://localhost:8050/api/account/info/3   
    - [kibana] 
