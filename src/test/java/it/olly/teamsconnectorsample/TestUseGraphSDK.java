package it.olly.teamsconnectorsample;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

/**
 * Graph API via SDK can only be used for 1.0 calls.. (or forking the
 * beta-repo).
 * 
 * @author alessio olivieri
 *
 */
@SpringBootTest
public class TestUseGraphSDK {
	public final String accessToken = "eyJ0eXAiOiJKV1QiLCJ...";

	@Autowired
	private MSClientHelper msClientHelper;

	@Test
	public void callGraphAPIWithToken() {
		GraphServiceClient<?> graphClient = GraphServiceClient.builder()
				.authenticationProvider(new IAuthenticationProvider() {
					@Override
					public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
						return CompletableFuture.completedFuture(accessToken);
					}
				}).buildClient();

		final User me = graphClient.me().buildRequest().get();
		System.out.println("> " + me.displayName + " - " + me.mail);
	}
}
