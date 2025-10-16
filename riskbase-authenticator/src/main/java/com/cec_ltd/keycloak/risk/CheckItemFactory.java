package com.cec_ltd.keycloak.risk;

import java.util.ArrayList;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.jpa.entities.RealmRiskBase;

public class CheckItemFactory {
	private ArrayList<CheckItem> instanceList = null;

	public CheckItemFactory(String realmId) {

//		Object entityManager;
//		TypedQuery<RealmRiskBase> query = entityManager.createQuery(
//				"SELECT r FROM RealmRiskBase r WHERE r.realm.id = :realmId AND r.enabled = true", RealmRiskBase.class);
//		query.setParameter("realmId", realmId);
//
//		List<RealmRiskBase> results = query.getResultList();
//		for (RealmRiskBase r : results) {
//			System.out.println("チェックID: " + r.getCheckid());
//			System.out.println("スコア: " + r.getValue());
//		}
		instanceList = new ArrayList<CheckItem>();
		CheckItem item = (CheckItem) new LoginCheckItem();
		item.setScore(20);
		instanceList.add(item);
		item = (CheckItem) new IPCheckItem();
		item.setScore(30);
		instanceList.add(item);
		item = (CheckItem) new LastLoginCheckItem();
		item.setScore(30);
		instanceList.add(item);
	}

	public int getScore(AuthenticationFlowContext context) {
		int result = 0;
		for (CheckItem instance : instanceList) {
			result += instance.getScore(context);
		}
		return result;
	}
}
