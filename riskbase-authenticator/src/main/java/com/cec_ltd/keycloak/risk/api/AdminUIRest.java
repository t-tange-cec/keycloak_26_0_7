package com.cec_ltd.keycloak.risk.api;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.hibernate.mapping.List;
import org.keycloak.models.jpa.entities.RealmRiskBase;

public class AdminUIRest {
	@PostMapping("/admin/riskbase")
	public ResponseEntity<?> saveRiskItems(@RequestBody List<RiskItemDto> items) {
	    for (RiskItemDto dto : items) {
	        RealmEntity realm = realmRepository.findById(dto.getRealmId());
	        RealmRiskBase entity = new RealmRiskBase();
	        entity.setId(UUID.randomUUID().toString());
	        entity.setRealm(realm);
	        entity.setCheckId(dto.getCheck_id());
	        entity.setValue(dto.getValue());
	        entity.setEnabled(dto.isEnabled());
	        riskBaseRepository.save(entity);
	    }
	    return ResponseEntity.ok().build();
	}
}
