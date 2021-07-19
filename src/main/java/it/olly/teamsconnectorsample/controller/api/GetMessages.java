package it.olly.teamsconnectorsample.controller.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
@RequestMapping({ "/getmessages", "/api/getmessages" }) // TODO leave just /api/getmessages (filtered)
public class GetMessages {
	private static final Logger logger = LoggerFactory.getLogger(GetMessages.class);

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Object get(@RequestParam String accessToken) throws IOException {
		logger.info("get messages invoked with token = " + accessToken);
		List<Map> ret = new ArrayList<>();

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", "Bearer " + accessToken);

		String getUri = "https://graph.microsoft.com/v1.0/me/messages";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<Map> msgResponse = restTemplate.exchange(getUri, HttpMethod.GET, entity, Map.class);
			logger.info("msgResponse: " + msgResponse);
			List<Map> msgs = (List<Map>) msgResponse.getBody().get("value");
			for (Map m : msgs) {
				ret.add(m);
			}
		} catch (Exception e) {
			logger.error("EEE", e);
			String emsg = e.getMessage();
			if (emsg.indexOf(":") > 0) {
				return emsg.substring(emsg.indexOf(":") + 1).trim();
			}
		}

		return ret;
	}
}
