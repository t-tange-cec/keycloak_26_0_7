package com.cec_ltd.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.apache.commons.codec.binary.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;

import com.cec_ltd.keycloak.risk.CheckItemFactory;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;

public class ConfigurationSecretQuestion implements Authenticator {
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		String qid = user.getFirstAttribute("qid");
		String enableRiskbase = realm.getAttribute("EnableRiskbase");
		if ("true".equalsIgnoreCase(enableRiskbase)) {
			if (StringUtil.isNullOrEmpty(qid)) {
				LoginFormsProvider provider = context.form();
				context.forceChallenge(provider.createForm("login-update-secret-question.ftl"));
			} else {
				// スキップまたは失敗
				context.attempted(); // スキップ扱い
			}
		} else {
			// スキップまたは失敗
			context.attempted(); // スキップ扱い
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		HttpRequest request = context.getHttpRequest();
		MultivaluedMap<String, String> map = request.getDecodedFormParameters();
		String answer = map.getFirst("secretAnswer");
		String qid = map.getFirst("qid");
		if (StringUtil.isNullOrEmpty(answer) || StringUtil.isNullOrEmpty(qid)) {
		    context.failure();
		    return;
		}		
		UserCredentialModel credential = UserCredentialModel.password(answer);
		UserCredentialManager credentialManager = context.getSession().userCredentialManager();
		credential.setType("secret-question");
		((KeycloakSession) context.getSession()).userCredentialManager().updateCredential(realm, user, credential);
		user.setSingleAttribute("qid", qid);
		context.success();
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
