package com.cec_ltd.keycloak.risk;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.Event;
import org.keycloak.events.EventQuery;
import org.keycloak.events.EventType;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;


/*
 * 最終ログインからの経過時間チェック
 * */

public class LastLoginCheckItem implements CheckItem {
	private int score;

	@Override
	public void setScore(int value) {
		score = value;
	}

	@Override
	public int getScore(AuthenticationFlowContext context) {
		int riskScore = 0;
		KeycloakSession session = context.getSession();
		EventStoreProvider eventStore = session.getProvider(EventStoreProvider.class);
		String userId = context.getUser().getId();
		String realmId = context.getRealm().getId();
        // 最新の LOGIN イベントを取得（降順で1件）
		List<Event> loginEvents = (List<Event>) eventStore.createQuery()
				.type(EventType.LOGIN)
				.realm(realmId)
				.user(userId)
				.orderByDescTime()
		        .maxResults(1)
		        .getResultStream();
		boolean expired = false;
		if (!loginEvents.isEmpty()) {
			Event lastLogin = loginEvents.get(0);
			Instant lastLoginTime = Instant.ofEpochMilli(lastLogin.getTime());
			Instant now = Instant.now();
			long daysSinceLogin = ChronoUnit.DAYS.between(lastLoginTime, now);
			expired = daysSinceLogin >= 14;
		} else {
			// ログイン履歴がない場合は「初回」とみなすか、期限切れとみなす
		    expired = true;
		}
		if (expired) {
			riskScore+= score;
		}		
		return riskScore;
	}
}
