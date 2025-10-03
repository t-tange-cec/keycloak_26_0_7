package com.cec_ltd.keycloak.authenticator;

import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.apache.commons.codec.binary.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;

import com.cec_ltd.keycloak.risk.CheckItemFactory;

import org.keycloak.models.RealmModel;

public class ConfigurationSecretQuestion implements Authenticator {
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		String qid = user.getFirstAttribute("qid");
		String enableRiskbase = realm.getAttribute("EnableRiskbase");
		Integer ThreashHold = Integer.valueOf(realm.getAttribute("ThreashHold"));
		if ("true".equalsIgnoreCase(enableRiskbase)) {
			if (StringUtil.isNullOrEmpty(qid)) {
				// フォームに渡す属性を設定
				LoginFormsProvider provider = context.form();
				// チャレンジ画面として表示
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
        String enableRiskbase = realm.getAttribute("EnableRiskbase");
		if ("true".equalsIgnoreCase(enableRiskbase)) {
			HttpRequest request = context.getHttpRequest();
			MultivaluedMap<String, String> map = request.getDecodedFormParameters();
			String answer = map.getFirst("secretAnswer");
			String qid = map.getFirst("qid");
			context.success();
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
