package com.cec_ltd.keycloak.risk.repository;

import org.keycloak.models.jpa.entities.QuestionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionMasterRepository extends JpaRepository<QuestionMaster, String> {
    List<QuestionMaster> findByRealmIdAndLocale(String realmId, String locale);
}