package it.olly.teamsconnectorsample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

@SpringBootTest
public class MSBetaCallsTest {
	public final String accessToken = "eyJ0eXAiOiJKV1...";

	@Autowired
	private MSClientHelper msClientHelper;

	@Test
	void listChats() throws JSONException {
		JSONObject json = msClientHelper.lowLevelGet("/beta/me/chats", accessToken);
		System.out.println("CHATS\n" + json);
		JSONArray chats = json.getJSONArray("value");
		for (int i = 0; i < chats.length(); i++) {
			JSONObject chatJO = chats.getJSONObject(i);
			System.out.println("CHAT - " + chatJO);
		}
	}
}
