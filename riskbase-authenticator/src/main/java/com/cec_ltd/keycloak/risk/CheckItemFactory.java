package com.cec_ltd.keycloak.risk;

import java.util.ArrayList;

import org.keycloak.authentication.AuthenticationFlowContext;

public class CheckItemFactory {
	private ArrayList<CheckItem> instanceList = null;

	public CheckItemFactory() {
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
			result+=instance.getScore(context);
		}
		return result;
	}
}
