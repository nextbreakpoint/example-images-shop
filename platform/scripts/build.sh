#!/bin/sh

set -e

eval $(minikube docker-env)

export REPOSITORY="integration"
export VERSION="1.0.0"
export BUILD="true"
export TEST="true"

#mvn clean deploy -s settings.xml -Dnexus=true
#
#pushd common
#mvn clean deploy -s settings.xml -Dnexus=true
#popd

pushd services/frontend
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/gateway
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/authentication
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/accounts
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs-notification-dispatcher
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs-command-consumer
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs-command-producer
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs-aggregate-fetcher
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs-event-consumer
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd

pushd services/designs-tile-renderer
if [ "$BUILD" == "true" ]; then
  sh build.sh $REPOSITORY $VERSION
fi
if [ "$TEST" == "true" ]; then
  mvn clean verify -Dminikube=true
fi
popd
