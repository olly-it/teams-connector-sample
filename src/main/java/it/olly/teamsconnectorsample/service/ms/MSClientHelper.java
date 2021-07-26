package it.olly.teamsconnectorsample.service.ms;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

/**
 * NOTE: sdk can be used for calls with version 1.0. for /beta/ requests,
 * lowLevel* must be used
 * 
 * @author alessio olivieri
 *
 */
@Component
public class MSClientHelper {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final static String GRAPHAPI_HOST = "https://graph.microsoft.com";

	@Autowired
	private RestTemplate restTemplate;

	public User getUser(String accessToken) {
		GraphServiceClient<?> graphClient = GraphServiceClient.builder()
				.authenticationProvider(new IAuthenticationProvider() {
					@Override
					public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
						return CompletableFuture.completedFuture(accessToken);
					}
				}).buildClient();
		return graphClient.me().buildRequest().get();
	}

	/**
	 * 
	 * @param api         e.g. "/beta/me/chats"
	 * @param accessToken
	 * @return a jsonElement (can be a primitive, an object or an array
	 */
	public JSONObject lowLevelGet(String api, String accessToken) {
		logger.info("lowLevelGet invoked [" + api + "] with token = " + accessToken);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = GRAPHAPI_HOST + api;
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		/*
		 * ResponseEntity<Map<String, Object>> msgResponse =
		 * restTemplate.exchange(getUri, HttpMethod.GET, entity, new
		 * ParameterizedTypeReference<Map<String, Object>>() { });
		 */
		ResponseEntity<String> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, String.class);

		logger.info("msgResponse: " + msgResponse);
		logger.info("msgResponse.body: " + msgResponse.getBody());
		try {
			return new JSONObject(msgResponse.getBody());
		} catch (Exception e) {
			logger.error("unable to convert json", e);
			throw new RuntimeException(e);
		}
	}
}
