package com.cec_ltd.keycloak.authenticator;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.UserModel;

import com.cec_ltd.keycloak.risk.CheckItemFactory;

import org.keycloak.models.RealmModel;

public class ConditionRiskbase implements Authenticator {
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		String currentIp = context.getConnection().getRemoteAddr();
		UserModel user = context.getUser();
        RealmModel realm = context.getRealm();
        String qid = user.getFirstAttribute("qid");
        String enableRiskbase = realm.getAttribute("EnableRiskbase");
        Integer ThreashHold = Integer.valueOf(realm.getAttribute("ThreashHold"));
        if ("true".equalsIgnoreCase(enableRiskbase)) {
            // リスクベース認証を実行（例：IPチェック、時間帯など）
            // ここでは単純に成功とする
        	CheckItemFactory factory=new CheckItemFactory();
        	Integer score=factory.getScore(context);
        	if (score>ThreashHold){
                context.success();
        	}else {
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
