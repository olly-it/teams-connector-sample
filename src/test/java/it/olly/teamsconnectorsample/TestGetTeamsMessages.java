package it.olly.teamsconnectorsample;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

@SpringBootTest
public class TestGetTeamsMessages {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RestTemplate restTemplate;

	@Test
	void teams_listJoinedTeams() {
		logger.info("teams_listJoinedTeams()");
		String accessToken = "eyJ0eXAiOiJKV1Qi...";

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = "https://graph.microsoft.com/v1.0/me/joinedTeams";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, String.class);
			logger.info("msgResponse: " + msgResponse);
			logger.info("body: " + msgResponse.getBody());
		} catch (Exception e) {
			logger.error("EEE", e);
		}
	}

	@Test
	void teams_listChats() {
		logger.info("teams_listChats()");
		String accessToken = "eyJ0eXAiOiJKV1Qi...";

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = "https://graph.microsoft.com/beta/me/chats";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, String.class);
			logger.info("msgResponse: " + msgResponse);
			logger.info("body: " + msgResponse.getBody());
		} catch (Exception e) {
			logger.error("EEE", e);
		}
	}

	// @Test
	void fullFlux_getAuthTokenAndDoRequest() {
		String clientId = "0580a610...";
		String clientSecret = null;
		List<String> scopes = List.of("openid", "offline_access", "https://graph.microsoft.com/mail.read");
		String authorizationCode = "0.AQkAZPggtVMv9U...";
		String redirectUri = "http://localhost:8080/msauthresponse";

		final AuthorizationCodeCredential authCodeCredential = new AuthorizationCodeCredentialBuilder()
				.clientId(clientId).clientSecret(clientSecret) // required for web apps, do not set for native apps
				.authorizationCode(authorizationCode).redirectUrl(redirectUri).build();

		final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes,
				authCodeCredential);

		GraphServiceClient<?> graphClient = GraphServiceClient.builder()
				.authenticationProvider(tokenCredentialAuthProvider).buildClient();

		final User me = graphClient.me().buildRequest().get();
		System.out.println("> " + me);

	}
}
