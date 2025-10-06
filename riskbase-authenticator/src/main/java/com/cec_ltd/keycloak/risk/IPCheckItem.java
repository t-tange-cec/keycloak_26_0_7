package com.cec_ltd.keycloak.risk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;
/*
 * IP履歴チェック
 * */

import com.cec_ltd.keycloak.authenticator.ConditionalRiskbase;

public class IPCheckItem implements CheckItem {
	private int score;
	private static final Logger logger = Logger.getLogger(ConditionalRiskbase.class);

	@Override
	public void setScore(int value) {
		score = value;
	}

	@Override
	public int getScore(AuthenticationFlowContext context) {
		int riskScore = 0;
		try {
			UserModel user = context.getUser();
			String currentIp = context.getConnection().getRemoteAddr();
			// 既知IPチェック（ユーザー属性に保存されたIP一覧と照合）
			List<String> knownIps = getKnownIps(user);
			if (knownIps.size() < 1) {
				// 初回ログイン
				riskScore += score;
			} else if (!knownIps.contains(currentIp)) {
				// 新規IP（既知でも未知でもない）
				riskScore += score;
			}
			return riskScore;
		} catch (Exception e) {
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			Logger.error("Authentication failed", e);
		}
	}

	private List<String> getKnownIps(UserModel user) {
		String ipList = user.getFirstAttribute("knownIps");
		if (ipList == null || ipList.isEmpty())
			return new ArrayList<>();
		return Arrays.asList(ipList.split(","));
	}

}
