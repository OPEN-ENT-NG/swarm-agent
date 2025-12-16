# swarm-agent

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw clean compile quarkus:dev -DNEXUS_USERNAME="$nexus_username" -DNEXUS_PASSWD="$nexus_password" -s settings.xml -U 
```

## Test agent and kubernetes dev

Si vous utilisez le kubernetes dev, vous devez installer kube et récupérer la configuration de votre cluster
vous pouvez lancer les commandes suivantes pour supprimer les pods et les services

Pour tester l'accès mysql

```bash
 mysql -h mysql-73dc4a5d-o998d46c3.database.cloud.ovh.net -P 20184 -u agent -p'5e67fz3AxDYtGPTLSpa8' --ssl-mode=REQUIRED defaultdb
```

```bash
# en se basant sur le contenu dans dev.sql
kubectl delete statefulset presta-doe -n services
kubectl delete statefulset wp-doe -n services

kubectl delete service presta-doe -n services
kubectl delete service wp-doe -n services

kubectl delete prestashops presta-doe -n services
kubectl delete wordpress wp-doe -n services
```

## Test agent and kubernetes locally

```bash
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube && rm minikube-linux-amd64

# pour démarrer
minikube start

# pour clean/stop r
minikube delete

# activer mode proxy 
kubectl proxy

On peut accéder à tout via 
http://localhost:8001/
```

### lancement mysql

```bash
docker-compose up -d
````

### Construire services namespace

```bash
# dans un dossier de votre choix
git clone https://git-pic1.support-ent.fr/CGI/app-swarm/swarm-k8s-deployment deploy
cd deploy
./namespace.sh
# et appliquer les opérateurs presta + wp
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package -DNEXUS_USERNAME="$nexus_username" -DNEXUS_PASSWD="$nexus_password" -s settings.xml 
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar -DNEXUS_USERNAME="$nexus_username" -DNEXUS_PASSWD="$nexus_password" -s settings.xml 
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative -DNEXUS_USERNAME="$nexus_username" -DNEXUS_PASSWD="$nexus_password" -s settings.xml 
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true -DNEXUS_USERNAME="$nexus_username" -DNEXUS_PASSWD="$nexus_password" -s settings.xml 
```

You can then execute your native executable with: `./target/swarm-agent-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
