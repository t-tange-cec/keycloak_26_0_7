package com.cec_ltd.keycloak.risk.api;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.keycloak.models.jpa.entities.RealmRiskBase;

public class AdminUIRest {
	@PostMapping("/admin/riskbase")
	public ResponseEntity<?> saveRiskItems(@RequestBody List<RiskItemDto> items) {
	    for (RiskItemDto dto : items) {
	        Object realmRepository;
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
