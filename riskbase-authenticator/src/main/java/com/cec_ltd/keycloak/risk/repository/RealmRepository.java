package com.cec_ltd.keycloak.risk.repository;

import org.keycloak.models.jpa.entities.RealmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealmRepository extends JpaRepository<RealmEntity, String> {
}