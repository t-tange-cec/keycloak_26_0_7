package com.cec_ltd.keycloak.risk;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Event;
import org.keycloak.events.EventQuery;
import org.keycloak.events.EventType;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import com.cec_ltd.keycloak.authenticator.ConditionalRiskbase;

/*
 * 最終ログインからの経過時間チェック
 * */

public class LastLoginCheckItem implements CheckItem {
	private int score;
	private static final Logger logger = Logger.getLogger(LastLoginCheckItem.class);


	@Override
	public void setScore(int value) {
		score = value;
	}

	@Override
	public int getScore(AuthenticationFlowContext context) {
		int riskScore = 0;
		try {
			KeycloakSession session = context.getSession();
			EventStoreProvider eventStore = session.getProvider(EventStoreProvider.class);
			String userId = context.getUser().getId();
			String realmId = context.getRealm().getId();
			// 最新の LOGIN イベントを取得（降順で1件）
			EventQuery query = eventStore.createQuery().type(EventType.LOGIN).realm(realmId).user(userId)
					.orderByDescTime();

			List<Event> loginEvents = query.getResultStream().collect(Collectors.toList());

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
				riskScore += score;
			}
		} catch (Exception e) {
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			logger.error("Authentication failed", e);
		}
		return riskScore;
	}
}
