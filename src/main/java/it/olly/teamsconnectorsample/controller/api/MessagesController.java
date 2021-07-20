package it.olly.teamsconnectorsample.controller.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin
@RequestMapping("/api/messages")
public class MessagesController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public void get(@RequestParam String accessToken, HttpServletResponse response) throws IOException {
		logger.info("get messages invoked with token = " + accessToken);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = "https://graph.microsoft.com/v1.0/me/messages";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, String.class);
			logger.info("msgResponse: " + msgResponse);
			response.getWriter().println(msgResponse.getBody());
		} catch (Exception e) {
			logger.error("EEE", e);
			String emsg = e.getMessage();
			if (emsg.indexOf(":") > 0) {
				response.getWriter().println(emsg.substring(emsg.indexOf(":") + 1).trim());
			}
		}
	}

	@GetMapping(path = "/chats", produces = MediaType.APPLICATION_JSON_VALUE)
	public void getChats(@RequestParam String accessToken, HttpServletResponse response) throws IOException {
		logger.info("get chats invoked with token = " + accessToken);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = "https://graph.microsoft.com/beta/me/chats";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, String.class);
			logger.info("msgResponse: " + msgResponse);
			response.getWriter().println(msgResponse.getBody());
		} catch (Exception e) {
			logger.error("EEE", e);
			String emsg = e.getMessage();
			if (emsg.indexOf(":") > 0) {
				response.getWriter().println(emsg.substring(emsg.indexOf(":") + 1).trim());
			}
		}
	}

	@GetMapping(path = "/chatmessages", produces = MediaType.APPLICATION_JSON_VALUE)
	public void getChatMessages(@RequestParam String accessToken, @RequestParam String chatId,
			HttpServletResponse response) throws IOException {
		logger.info("get chatmessages invoked with chatId = " + chatId + " and token = " + accessToken);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = "https://graph.microsoft.com/beta/me/chats/" + chatId + "/messages";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, String.class);
			logger.info("msgResponse: " + msgResponse);
			response.getWriter().println(msgResponse.getBody());
		} catch (Exception e) {
			logger.error("EEE", e);
			String emsg = e.getMessage();
			if (emsg.indexOf(":") > 0) {
				response.getWriter().println(emsg.substring(emsg.indexOf(":") + 1).trim());
			}
		}
	}
}
