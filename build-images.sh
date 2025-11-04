#!/bin/bash
set +X

function unpackJar() {
  FOLDER=$1
  NAME=$2
  VERSION=$3

  CURRENT=$(pwd)

  cd $FOLDER/target
  java -jar -Djarmode=layertools ${NAME}-${VERSION}.jar extract
#  java -jar -Djarmode=tools ${NAME}-${VERSION}.jar extract --layers --launcher

  cd $CURRENT
}

function buildDockerImage() {
  FOLDER=$1
  NAME=$2

  docker build -f ./build/Dockerfile \
    --build-arg JAR_FOLDER=${FOLDER}/target \
    -t ${NAME}:latest .
}

APP_VERSION=0.0.1-SNAPSHOT

echo "Building JAR files"
mvn clean package -DskipTests

echo "Unpacking JARs"
unpackJar accounts accounts ${APP_VERSION}
unpackJar payments payments ${APP_VERSION}
unpackJar apigw apigw ${APP_VERSION}
unpackJar eureka-server eureka-server ${APP_VERSION}
unpackJar fraud-detection fraud-detection ${APP_VERSION}
unpackJar tx-event-publisher tx-event-publisher ${APP_VERSION}
unpackJar dbmigrations dbmigrations ${APP_VERSION}

echo "Building Docker image"
buildDockerImage accounts app/accounts ${APP_VERSION}
buildDockerImage payments app/payments ${APP_VERSION}
buildDockerImage apigw app/apigw ${APP_VERSION}
buildDockerImage eureka-server app/eureka-server ${APP_VERSION}
buildDockerImage fraud-detection app/fraud-detection ${APP_VERSION}
buildDockerImage tx-event-publisher app/tx-event-publisher ${APP_VERSION}
buildDockerImage dbmigrations app/dbmigrations ${APP_VERSION}