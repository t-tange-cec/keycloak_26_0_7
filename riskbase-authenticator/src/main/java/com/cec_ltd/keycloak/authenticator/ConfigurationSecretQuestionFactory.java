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

@AutoService(AuthenticatorFactory.class)
public class ConfigurationSecretQuestionFactory implements AuthenticatorFactory {
	public static final String PROVIDER_ID = "configuration-secret-question";

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Configration Secret Question";
	}

	@Override
	public String getReferenceCategory() {
		return "risk";
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
		return new ConfigurationSecretQuestion();
	}

	@Override
	public String getHelpText() {
		return "Configration Secret Question";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return null;
	}

}
