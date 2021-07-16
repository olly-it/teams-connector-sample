package it.olly.teamsconnectorsample;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

public class TestGetTeamsMessages {

	@Test
	void doTestWithAuthorizationCode() {
		// TODO not working this

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
