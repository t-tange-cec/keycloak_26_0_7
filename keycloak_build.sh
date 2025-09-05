#!/usr/bin/bash

export REPO_NAME=keycloak_26_0_7
export REPO_USER=t-tange-cec
export SOURCE_URL=https://github.com/${REPO_USER}/${REPO_NAME}.git
export CLONE_DIR=${HOME}/${REPO_NAME}/keycloak-26.0.7
export MAVEN_OPTS="-Xmx4g -Xms512m"
export NODE_OPTIONS="--max-old-space-size=4096"
export BUILD_DIR=${HOME}/keycloak


rm -rf ${HOME}/${REPO_NAME}
rm -rf ${BUILD_DIR}
git clone ${SOURCE_URL}
mv  ${CLONE_DIR} ${BUILD_DIR}

pushd ${BUILD_DIR}
 mvn clean install -DskipTests &> ${HOME}/build.log
popd
