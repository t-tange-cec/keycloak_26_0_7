#!/usr/bin/bash

export KC_BOOTSTRAP_ADMIN_USERNAME=admin
export KC_BOOTSTRAP_ADMIN_PASSWORD=admin

rm -rf /home/keycloak/keycloak-26.0.7
tar zxvf keycloak/quarkus/dist/target/keycloak-26.0.7.tar.gz

/home/keycloak/keycloak-26.0.7/bin/kc.sh start-dev

