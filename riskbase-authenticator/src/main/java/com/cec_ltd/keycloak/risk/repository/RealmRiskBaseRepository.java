package com.cec_ltd.keycloak.risk.repository;

import java.util.List;

import org.keycloak.models.jpa.entities.RealmRiskBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealmRiskBaseRepository extends JpaRepository<RealmRiskBase, String> {
	List<RealmRiskBase> findByRealm(String realmId);
}