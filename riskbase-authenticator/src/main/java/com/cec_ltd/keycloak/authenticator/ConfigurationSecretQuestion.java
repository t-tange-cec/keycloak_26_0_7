package com.cec_ltd.keycloak.authenticator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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

import org.jboss.logging.Logger;

public class ConfigurationSecretQuestion implements Authenticator {
	private static final Logger logger = Logger.getLogger(ConfigurationSecretQuestion.class);
	private static String FORM_NAME = "login-update-secret-question.ftl";

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		try {
			String qid = user.getFirstAttribute("qid");
			logger.info("start");
			if (StringUtil.isNullOrEmpty(qid)) {
				logger.info("qid: null");
				LoginFormsProvider provider = context.form();
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
		try {
			logger.info("secretAnswer: " + answer);
			if (StringUtil.isNullOrEmpty(answer) || StringUtil.isNullOrEmpty(qid)) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
						context.form().setError("質問または回答が入力されていません").createForm(FORM_NAME));
				return;
			}
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
			SecretQuestionCredentialModel model = SecretQuestionCredentialModel.createFromValues(
				    "pbkdf2-sha256", hashed, 27500, Map.of("questionId", List.of(qid)), encodedAnswer
				);
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
