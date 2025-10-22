package com.cec_ltd.keycloak.authenticator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.binary.StringUtils;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.Base64;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;

import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;

import com.cec_ltd.keycloak.credential.SecretQuestionCredentialModel;
import com.cec_ltd.keycloak.risk.CheckItemFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

public class ConfigurationSecretQuestion implements Authenticator {
	private static final Logger logger = Logger.getLogger(ConfigurationSecretQuestion.class);
	private static String FORM_NAME = "login-update-secret-question.ftl";
	private static Map<String, String> messagelist = new HashMap<>();
	private static List<Map<String, String>> options = new ArrayList<>();

	private void getMessageList(RealmModel realm, Locale locale) {
		messagelist.clear();
		options.clear();
		String json = realm.getAttribute("secretQuestions");
		logger.info(json);
		ObjectMapper mapper = new ObjectMapper();
		boolean loaded = false;
		try {
			List<Map<String, String>> questions = mapper.readValue(json,
					new TypeReference<List<Map<String, String>>>() {
					});
			for (Map<String, String> q : questions) {
				messagelist.put(q.get("id"), q.get(locale.toString()));
			}
			loaded = true;
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!loaded) {
			if (locale.toString().equals("ja")) {
				messagelist.put("PI001", "\u3042\u306a\u305f\u306e\u597d\u304d\u306a\u6620\u753b\u306f\uff1f");
				messagelist.put("PI002", "\u3042\u306a\u305f\u306e\u30da\u30c3\u30c8\u306e\u540d\u524d\u306f\uff1f");
				messagelist.put("PI003", "\u3042\u306a\u305f\u306e\u6bcd\u89aa\u306e\u65e7\u59d3\u306f\uff1f");
				messagelist.put("PI004", "\u3042\u306a\u305f\u306e\u51fa\u8eab\u5730\u306f\uff1f");
				messagelist.put("PI005",
						"\u3042\u306a\u305f\u306e\u597d\u304d\u306a\u30b9\u30dd\u30fc\u30c4\u30c1\u30fc\u30e0\u306f\uff1f");
				messagelist.put("PI006", "\u521d\u3081\u3066\u65c5\u884c\u3057\u305f\u5834\u6240\u306f\uff1f");
			} else {
				messagelist.put("PI001", "What's your favorite movie?");
				messagelist.put("PI002", "What's your pet's name?");
				messagelist.put("PI003", "What's your mother's maiden name?");
				messagelist.put("PI004", "Where are you from?");
				messagelist.put("PI005", "What's your favorite sports team?");
				messagelist.put("PI006", "Where was the first place you traveled to?");
			}
		}
		for (Map.Entry<String, String> entry : messagelist.entrySet()) {
			Map<String, String> message = new HashMap<>();
			message.put("id", entry.getKey());
			message.put("message", entry.getValue());
			options.add(message);
		}
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		String acceptLanguage = context.getHttpRequest().getHttpHeaders().getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
		Locale locale = Locale.forLanguageTag(acceptLanguage.split(",")[0]);
		try {
			String qid = user.getFirstAttribute("qid");
			logger.info("start");
			if (StringUtil.isNullOrEmpty(qid)) {
				getMessageList(realm, locale);
				logger.info("qid: null");
				logger.info("locale: " + locale.toString());
				LoginFormsProvider provider = context.form();
				provider.setAttribute("selectionOptions", options);
				logger.info(FORM_NAME);
				context.forceChallenge(provider.createForm(FORM_NAME));
				return;
			}
			logger.info("success");
			context.success();
		} catch (Exception e) {
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			logger.error("Authentication failed", e);
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
		logger.info("qid: " + qid);
		SecretQuestionCredentialModel model = null;
		try {
			logger.info("secretAnswer: " + answer);
			if (StringUtil.isNullOrEmpty(answer) || StringUtil.isNullOrEmpty(qid)) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
						context.form().setError("質問または回答が入力されていません").createForm(FORM_NAME));
				return;
			}
			if (false) {
				logger.info("phase 1 ");
				MessageDigest digest = MessageDigest.getInstance("PBKDF2WithHmacSHA256");
				SecureRandom random = new SecureRandom();
				byte[] salt = new byte[16];
				random.nextBytes(salt);
				digest.update(salt);

				SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				PBEKeySpec spec = new PBEKeySpec(answer.toCharArray(), salt, 27500, 256);
				byte[] hashed = skf.generateSecret(spec).getEncoded();

				String encodedAnswer = Base64.encodeBytes(hashed);
				logger.info("phase 2 ");
				model = SecretQuestionCredentialModel.createFromValues("pbkdf2-sha256", salt, 27500,
						Map.of("questionId", List.of(qid)), encodedAnswer);
			} else {
				model = SecretQuestionCredentialModel.createFromValues("", null, 27500,
						Map.of("questionId", List.of(qid)), answer);
			}
			logger.info("phase 3 ");
			CredentialProvider<CredentialModel> provider = (CredentialProvider<CredentialModel>) context.getSession()
					.getProvider(CredentialProvider.class, "secret-question");
			provider.createCredential(realm, user, model);
			user.setSingleAttribute("qid", qid);
			logger.info("success");
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
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
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
