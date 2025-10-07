package com.cec_ltd.keycloak.authenticator;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;

import com.cec_ltd.keycloak.risk.CheckItemFactory;

import org.keycloak.models.RealmModel;
import org.jboss.logging.Logger;

public class ConditionalRiskbase implements Authenticator {
	private static final Logger logger = Logger.getLogger(ConditionalRiskbase.class);

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		String currentIp = context.getConnection().getRemoteAddr();
		UserModel user = context.getUser();
		RealmModel realm = context.getRealm();
		String qid = user.getFirstAttribute("qid");
		String enableRiskbase = realm.getAttribute("EnableRiskbase");
		try {
			if (StringUtil.isNullOrEmpty(enableRiskbase)) {
				enableRiskbase = "true";
			}
			logger.info("enableRiskbase: " + enableRiskbase);
			if ("true".equalsIgnoreCase(enableRiskbase)) {
				String strThreahhold = realm.getAttribute("Threashold");
				if (StringUtil.isNullOrEmpty(strThreahhold)) {
					strThreahhold = "50";
				}
				Integer threashold = Integer.valueOf(strThreahhold);
				logger.info("threshhold:" + strThreahhold);
				// リスクベース認証を実行（例：IPチェック、時間帯など）
				// ここでは単純に成功とする
				CheckItemFactory factory = new CheckItemFactory();
				Integer score = factory.getScore(context);
				logger.info("score:" + Integer.toString(score));
				String riskLevel = (score >= threashold) ? "high" : "low";
				logger.info("riskLevel:" + riskLevel);
				context.getAuthenticationSession().setAuthNote("riskLevel", riskLevel);
			}
			logger.info("success:");
			context.success();
		} catch (Exception e) {
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			logger.error("Authentication failed", e);
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
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
