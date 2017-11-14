
Local setup.

```minikube start``` - start the minikube in a vm

```eval $(minikube docker-env)``` - to build and push docker images to kubenetes docker registry
```eval $(minikube docker-env) -u``` - get back to local docker registry.

```sh ./build.sh``` builds the docker images and pushes to minikube docker registry.

```sh ./run.sh``` installs the kubectl pods/services.    

```kubectl get deployments,pods,services,ing``` - show all resources deployed. 

Ingress for routing services.
```minikube addons enable ingress```