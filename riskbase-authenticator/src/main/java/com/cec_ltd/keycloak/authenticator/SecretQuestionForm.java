package com.cec_ltd.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;

import java.util.List;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;

public class SecretQuestionForm implements Authenticator {
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		String qid = user.getFirstAttribute("qid");

		// ユーザー属性から秘密の質問を取得（例：user.getFirstAttribute("secretQuestion")）
		String question = user.getFirstAttribute("qid");

		// フォームに渡す属性を設定
		LoginFormsProvider provider = context.form();
		provider.setAttribute("qid", question);

		// チャレンジ画面として表示
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

		CredentialProvider<CredentialModel> provider = 
		    (CredentialProvider<CredentialModel>) context.getSession()
		        .getProvider(CredentialProvider.class, "secret-question");
		UserCredentialModel input = UserCredentialModel.secretQuestion(answer);
		boolean valid = provider.isValid(context.getRealm(), context.getUser(), input);
		if (valid) {
		    context.success();
		} else {
		    context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
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
