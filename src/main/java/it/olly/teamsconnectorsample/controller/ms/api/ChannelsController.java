package it.olly.teamsconnectorsample.controller.ms.api;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

@RestController
@CrossOrigin
@RequestMapping("/api/channels")
public class ChannelsController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MSClientHelper msClientHelper;

	@GetMapping(path = "/send")
	public void sendMessage(@RequestParam String teamId, @RequestParam String channelId,
			@RequestParam String accessToken, @RequestParam String message) throws IOException {
		logger.info("sendMessage [" + channelId + "] invoked");
		JSONObject json = new JSONObject().put("body", new JSONObject().put("content", message));
		String api = "/beta/teams/" + teamId + "/channels/" + channelId + "/messages";
		JSONObject lowLevelPost = msClientHelper.lowLevelPost(api, accessToken, json);
		logger.info("sendMessage done - " + lowLevelPost);
	}
}
