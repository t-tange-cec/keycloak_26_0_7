package com.cec_ltd.keycloak.authenticator;

import com.google.auto.service.AutoService;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.AuthenticationExecutionModel;
import org.jboss.logging.Logger;


@AutoService(AuthenticatorFactory.class)
public class ConditionalRiskbaseFactory implements AuthenticatorFactory {
	public static final String PROVIDER_ID = "cond-riskbase";
	private static final Logger logger = Logger.getLogger(ConditionalRiskbaseFactory.class);

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Conditional Riskbase";
	}

	@Override
	public String getReferenceCategory() {
		return "risk";
	}
	public boolean isConditional() {
	    return true;
	}
	@Override
	public boolean isConfigurable() {
		return false;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return new AuthenticationExecutionModel.Requirement[] { AuthenticationExecutionModel.Requirement.REQUIRED,
				AuthenticationExecutionModel.Requirement.ALTERNATIVE,
				AuthenticationExecutionModel.Requirement.DISABLED };
	}

	@Override
	public void init(Config.Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		return new ConditionalRiskbase();
	}

	@Override
	public String getHelpText() {
		return "Conditional Riskbase check realm attribute EnableRiskbase has true";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return null;
	}

}
