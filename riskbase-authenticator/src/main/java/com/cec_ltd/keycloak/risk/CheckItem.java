package com.cec_ltd.keycloak.risk;

import org.keycloak.authentication.AuthenticationFlowContext;

public interface CheckItem {
	public void setScore(int value);
	public int getScore(AuthenticationFlowContext context);
}
