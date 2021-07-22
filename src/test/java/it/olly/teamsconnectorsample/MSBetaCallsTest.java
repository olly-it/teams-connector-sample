package it.olly.teamsconnectorsample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;
import it.olly.utils.JsonArray;
import it.olly.utils.JsonObject;

@SpringBootTest
public class MSBetaCallsTest {
	public final String accessToken = "eyJ0eXAiOiJKV1...";

	@Autowired
	private MSClientHelper msClientHelper;

	@Test
	void listChats() {
		JsonObject json = msClientHelper.lowLevelGet("/beta/me/chats", accessToken);
		System.out.println("CHATS\n" + json);
		JsonArray chats = json.getJsonArray("value");
		for (int i = 0; i < chats.size(); i++) {
			JsonObject chatJO = chats.getJsonObject(i);
			System.out.println("CHAT - " + chatJO);
		}
	}
}
