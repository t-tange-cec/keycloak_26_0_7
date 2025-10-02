package com.cec_ltd.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;

public class SecretQuestionForm implements Authenticator {
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		String qid = user.getFirstAttribute("qid");
		String enableRiskbase = realm.getAttribute("EnableRiskbase");
		if ("true".equalsIgnoreCase(enableRiskbase)) {
			// リスクベース認証を実行（例：IPチェック、時間帯など）
			// ここでは単純に成功とする

			// ユーザー属性から秘密の質問を取得（例：user.getFirstAttribute("secretQuestion")）
			String question = user.getFirstAttribute("qid");

			// フォームに渡す属性を設定
			LoginFormsProvider provider = context.form();
			provider.setAttribute("qid", question);

			// チャレンジ画面として表示
			context.forceChallenge(provider.createForm("login-secret-question.ftl"));
		} else {
			context.success();
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		RealmModel realm = context.getRealm();
		String enableRiskbase = realm.getAttribute("EnableRiskbase");
		if ("true".equalsIgnoreCase(enableRiskbase)) {
			HttpRequest request=context.getHttpRequest();
			MultivaluedMap<String, String> map =　request.getDecodedFormParameters();
			String answer = map.getFirst("secretAnswer");

			// ユーザー属性と照合
			String expected = context.getUser().getFirstAttribute("secretAnswer");

			if (expected != null && expected.equalsIgnoreCase(answer)) {
				context.success();
			} else {
				context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
			}
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
