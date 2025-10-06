package com.cec_ltd.keycloak.risk;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.Event;
import org.keycloak.events.EventQuery;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
/*
 * 認証失敗チェック
 * */

public class LoginCheckItem implements CheckItem {
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

		ZonedDateTime todayStart = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDate()
				.atStartOfDay(ZoneId.of("Asia/Tokyo"));
		ZonedDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);

		Date from = Date.from(todayStart.toInstant());
		Date to = Date.from(todayEnd.toInstant());
		EventQuery query = eventStore.createQuery().type(EventType.LOGIN_ERROR).user(userId).fromDate(from).toDate(to);

		List<Event> todayEvents = query.getResultStream().collect(Collectors.toList());

		riskScore += todayEvents.size() * score;

		return riskScore;
	}

}
