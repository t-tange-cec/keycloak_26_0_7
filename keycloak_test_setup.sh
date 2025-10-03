#!/usr/bin/bash

export KC_BOOTSTRAP_ADMIN_USERNAME=admin
export KC_BOOTSTRAP_ADMIN_PASSWORD=admin

rm -rf /home/keycloak/keycloak-26.0.7
tar zxvf keycloak/quarkus/dist/target/keycloak-26.0.7.tar.gz
cp /home/keycloak/keycloak_26_0_7/riskbase-authenticator/target/riskbase-authenticator-0.0.1-SNAPSHOT.jar /home/keycloak/keycloak-26.0.7/providers
/home/keycloak/keycloak-26.0.7/bin/kc.sh start-dev

