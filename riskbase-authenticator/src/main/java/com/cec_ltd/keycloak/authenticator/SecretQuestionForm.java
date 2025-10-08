package com.cec_ltd.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;

import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;

public class SecretQuestionForm implements Authenticator {
	private static final Logger logger = Logger.getLogger(SecretQuestionForm.class);

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		RealmModel realm = context.getRealm();
		String enableRiskbase = context.getAuthenticationSession().getAuthNote("EnableRiskbase");
		String riskLevel = context.getAuthenticationSession().getAuthNote("riskLevel");
		if (StringUtil.isNullOrEmpty(enableRiskbase)) {
			enableRiskbase = "false";
		}
		if ("false".equals(enableRiskbase)) {
			logger.info("enableRiskbase:false");
			context.success();
			return;
		}
		if (!"high".equals(riskLevel)) {
			logger.info("success:");
			context.success();
			return;
		}
		UserModel user = context.getUser();
		String qid = user.getFirstAttribute("qid");
		LoginFormsProvider provider = context.form();
		provider.setAttribute("qid", qid);
		provider.setAttribute("message", qid);
		context.forceChallenge(provider.createForm("login-secret-question.ftl"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void action(AuthenticationFlowContext context) {
		RealmModel realm = context.getRealm();
		UserModel user = context.getUser();
		HttpRequest request = context.getHttpRequest();
		MultivaluedMap<String, String> map = request.getDecodedFormParameters();
		String answer = map.getFirst("secretAnswer");
//		CredentialInput input = new CredentialInput() {
//		public String getType() {
//			return "secret-question";
//		}
//
//		public String getChallengeResponse() {
//			return answer;
//		}
//
//		public String getCredentialId() {
//			return null;
//		}
//	};
		CredentialInput input = (CredentialInput) new UserCredentialModel() {
			public String getType() {
				return "secret-question";
			}

			public String getChallengeResponse() {
				return answer;
			}

			public String getCredentialId() {
				return UUID.randomUUID().toString();
			}
		};
		try {
			logger.info("answer:" + answer);
			CredentialInputValidator provider = (CredentialInputValidator) context.getSession()
					.getProvider(CredentialProvider.class, "secret-question");
			if (provider == null) {
				logger.info("provider:null");
			} else {
				logger.info("provider:not null");
			}
			boolean valid = provider.isValid(realm, user, input);
			if (!valid) {
				logger.info("failure");
				context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
				return;
			}
			logger.info("success:");
			context.success();
		} catch (Exception e) {
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			logger.error("Authentication failed", e);
		}
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(org.keycloak.models.KeycloakSession session, org.keycloak.models.RealmModel realm,
			UserModel user) {
		return true;
	}

	@Override
	public void setRequiredActions(org.keycloak.models.KeycloakSession session, org.keycloak.models.RealmModel realm,
			UserModel user) {
	}

	@Override
	public void close() {
	}

}
