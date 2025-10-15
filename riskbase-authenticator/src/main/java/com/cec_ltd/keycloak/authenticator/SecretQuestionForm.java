package com.cec_ltd.keycloak.authenticator;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.Base64;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;

public class SecretQuestionForm implements Authenticator {
	private static final Logger logger = Logger.getLogger(SecretQuestionForm.class);
	private static String FORM_NAME = "login-secret-question.ftl";

	private static Map<String, String> messagelist = new HashMap<>();

	private void getMessageList(Locale locale){
		messagelist.clear();
		if (locale.toString().equals("ja")) {
			messagelist.put("PI001", "あなたの好きな映画は？");
			messagelist.put("PI002", "あなたのペットの名前は？");
			messagelist.put("PI003", "あなたの母親の旧姓は？");
			messagelist.put("PI004", "あなたの出身地は？");
			messagelist.put("PI005", "あなたの好きなスポーツチームは？");
			messagelist.put("PI006", "初めて旅行した場所は？");
		} else {
			messagelist.put("PI001", "What's your favorite movie?");
			messagelist.put("PI002", "What's your pet's name?");
			messagelist.put("PI003", "What's your mother's maiden name?");
			messagelist.put("PI004", "Where are you from?");
			messagelist.put("PI005", "What's your favorite sports team?");
			messagelist.put("PI006", "Where was the first place you traveled to?");
		}
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		RealmModel realm = context.getRealm();
		String acceptLanguage = context.getHttpRequest().getHttpHeaders().getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
		Locale locale = Locale.forLanguageTag(acceptLanguage.split(",")[0]);	
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
		logger.info("locale: "+locale.toString());
		getMessageList(locale);
		UserModel user = context.getUser();
		String qid = user.getFirstAttribute("qid");
		String message = messagelist.get(qid);
		logger.info("qid: "+qid);
		logger.info("message: "+message);
		LoginFormsProvider provider = context.form();
		provider.setAttribute("qid", qid);
		provider.setAttribute("question", message);
		logger.info(FORM_NAME);
		context.forceChallenge(provider.createForm(FORM_NAME));
		return;
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		RealmModel realm = context.getRealm();
		UserModel user = context.getUser();
		HttpRequest request = context.getHttpRequest();
		MultivaluedMap<String, String> map = request.getDecodedFormParameters();
		String answer = map.getFirst("secretAnswer");
		String id = user.getId();
		CredentialInput input = (CredentialInput) new UserCredentialModel() {
			public String getType() {
				return "secret-question";
			}

			public String getChallengeResponse() {
				return answer;
			}

			public String getCredentialId() {
				return id;
			}
		};
		try {
			CredentialInputValidator provider = (CredentialInputValidator) context.getSession()
					.getProvider(CredentialProvider.class, "secret-question");
			if (provider == null) {
				logger.info("provider:null");
			} else {
				logger.info("provider:not null");
			}
			boolean valid = provider.isValid(realm, user, input);
//			if (!valid) {
//				logger.info("failure");
//				context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
//				return;
//			}
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
