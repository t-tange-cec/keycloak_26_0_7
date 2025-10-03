#!/usr/bin/bash

export REPO_NAME=keycloak_26_0_7
export REPO_USER=t-tange-cec
#export BRANCH_OPT="-b develop"
export BRANCH_OPT="-b Riskbase_4"
#export BRANCH_OPT="-b main"
export SOURCE_URL=https://t-tange-cec:${GITHUB_PAT}@github.com/${REPO_USER}/${REPO_NAME}.git
export CLONE_DIR=${HOME}/${REPO_NAME}/keycloak-26.0.7
export MAVEN_OPTS="-Xmx4g -Xms512m"
export NODE_OPTIONS="--max-old-space-size=4096"
export BUILD_DIR=${HOME}/keycloak
export DIST_DIR=${HOME}/keycloak/distribution


rm -rf ${HOME}/${REPO_NAME}
rm -rf ${BUILD_DIR}
git clone ${BRANCH_OPT} ${SOURCE_URL}
mv  ${CLONE_DIR} ${BUILD_DIR}

pushd ${BUILD_DIR}
 mvn clean install -DskipTests &> ${HOME}/build.log
popd
pushd ${HOME}/${REPO_NAME}/riskbase-authenticator
 mvn install &> ${HOME}/build_spi.log
popd
pushd ${DIST_DIR}
 mvn install -DskipTests &> ${HOME}/build_dist.log
popd

#rm -rf ${HOME}/${REPO_NAME}
