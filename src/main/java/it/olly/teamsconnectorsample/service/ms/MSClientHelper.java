package it.olly.teamsconnectorsample.service.ms;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MailFolderCollectionPage;
import com.microsoft.graph.requests.MessageCollectionPage;

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
	// ---------------------------------------------- 2021-07-20 T 17:00:00.0000000Z
	private final static String MS_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public final static int WEBHOOK_MINUTES_EXPIRATION = 10;

	@Value("${ms.notification.receiver}")
	public String notificationResponseUrl;

	@Autowired
	private RestTemplate restTemplate;
	
	private class MyAuthProvider implements IAuthenticationProvider{
		private String token; 
		public MyAuthProvider(String token) {
			this.token = token;
		}
		public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
			return CompletableFuture.completedFuture(token);
		}
	}

	public User getUser(String accessToken) {
		GraphServiceClient<?> graphClient = GraphServiceClient.builder()
				.authenticationProvider(new MyAuthProvider(accessToken)).buildClient();
		return graphClient.me().buildRequest().get();
	}
	
	public MailFolderCollectionPage getEmailFolders(String accessToken) {
		GraphServiceClient<?> graphClient = GraphServiceClient.builder()
				.authenticationProvider(new MyAuthProvider(accessToken)).buildClient();
		
		return graphClient.me().mailFolders().buildRequest().get();
	}
	
	public MessageCollectionPage getEmails(String accessToken, String folderId) {
		GraphServiceClient<?> graphClient = GraphServiceClient.builder()
				.authenticationProvider(new MyAuthProvider(accessToken)).buildClient();
		return graphClient.me().mailFolders(folderId).messages().buildRequest().get();
	}


	/**
	 * 
	 * @param api         e.g. "/beta/chats/{chat-id}/messages"
	 * @param accessToken
	 * @return JSONObject
	 */
	public JSONObject lowLevelPost(String api, String accessToken, JSONObject body) {
		logger.info("lowLevelPost invoked [" + api + "] with token = " + accessToken);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String uri = GRAPHAPI_HOST + api;

		// body
		String bodyS = body.toString();
		logger.info("posting - " + bodyS);

		// prepare request
		RequestEntity<String> entity = RequestEntity //
				.post(uri) //
				.header("Authorization", "Bearer " + accessToken) //
				.contentType(MediaType.APPLICATION_JSON) //
				.body(bodyS);

		// execute
		ResponseEntity<String> msgResponse = restTemplate.exchange(entity, String.class);

		logger.info("msgResponse: " + msgResponse);
		logger.info("msgResponse.body: " + msgResponse.getBody());
		try {
			return new JSONObject(msgResponse.getBody());
		} catch (Exception e) {
			logger.error("unable to convert json", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param api         e.g. "/beta/me/chats"
	 * @param accessToken
	 * @return JSONObject
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

	public boolean alreadySubscribedToWebhook(String webhookResource, String accessToken) {
		logger.info("AlreadySubscribedToWebhook? " + webhookResource);
		String uri = GRAPHAPI_HOST + "/v1.0/subscriptions";

		logger.info("get request");

		// prepare request
		RequestEntity<Void> entity = RequestEntity //
				.get(uri) //
				.header("Authorization", "Bearer " + accessToken).build();

		// execute get
		ResponseEntity<String> msgResponse = restTemplate.exchange(entity, String.class);

		logger.info("msgResponse: " + msgResponse);
		logger.info("msgResponse.body: " + msgResponse.getBody());

		JSONObject json = new JSONObject(msgResponse.getBody());
		JSONArray jsonArray = json.getJSONArray("value");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject element = jsonArray.optJSONObject(i);
			if (element != null) {
				String resource = element.optString("resource");
				if (resource != null && resource.equals(webhookResource))
					return true;
			}

		}
		return false;
	}

	public void subscribeToWebhook(String webhookResource, String accessToken) {
		logger.info("subscribe to webhook: " + webhookResource);
		String uri = GRAPHAPI_HOST + "/v1.0/subscriptions";

		// body
		JSONObject body = new JSONObject();
		body.put("changeType", "created");
		body.put("notificationUrl", notificationResponseUrl);
		body.put("resource", webhookResource);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, WEBHOOK_MINUTES_EXPIRATION);
		body.put("expirationDateTime", toMSDateTime(cal.getTime()));
		String bodyS = body.toString();
		logger.info("posting - " + bodyS);

		// prepare request
		RequestEntity<String> entity = RequestEntity //
				.post(uri) //
				.header("Authorization", "Bearer " + accessToken) //
				.contentType(MediaType.APPLICATION_JSON) //
				.body(bodyS);

		// execute
		ResponseEntity<String> msgResponse = restTemplate.exchange(entity, String.class);

		logger.info("msgResponse: " + msgResponse);
		logger.info("msgResponse.body: " + msgResponse.getBody());
	}

	// UTILS
	public static final String toMSDateTime(Date dt) {
		DateFormat df = new SimpleDateFormat(MS_DATETIME_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(dt);
	}

	public static final Date fromMSDateTime(String str) throws ParseException {
		DateFormat df = new SimpleDateFormat(MS_DATETIME_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.parse(str);
	}

	public static void main(String[] args) {
		System.out.println("> " + toMSDateTime(new Date()));
	}

}
