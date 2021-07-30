package it.olly.teamsconnectorsample.controller.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

@RestController
@CrossOrigin
@RequestMapping("/api/stream")
public class StreamController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HazelcastInstance hazelcast;

	@Autowired
	private MSClientHelper msClientHelper;

	@GetMapping(path = "/chat")
	public void getChatStream(@RequestParam String chatId, @RequestParam String accessToken,
			HttpServletResponse response) throws IOException {
		response.setContentType("text/event-stream");
		response.setCharacterEncoding("utf-8");
		logger.info("getChatStream [" + chatId + "] invoked");
		IQueue<String> queue = hazelcast.getQueue("chat|" + chatId);
		try {
			String poll = null;
			do {
				poll = queue.poll(60, TimeUnit.SECONDS);
				if (poll != null) {
					logger.info("getChatStream [" + chatId + "] - GOT MESSAGE");
					JSONObject notif = new JSONObject(poll);

					// ask microsoft single chat Message
					JSONObject msgJO = msClientHelper.lowLevelGet(
							"/beta/me/chats/" + chatId + "/messages/" + notif.getString("messageId"), accessToken);
					JSONObject fromJO = msgJO.optJSONObject("from");
					String from = fromJO != null ? fromJO.getJSONObject("user").getString("displayName") : "[empty]";
					JSONObject body = msgJO.getJSONObject("body");

					// reply to browser
					JSONObject reply = new JSONObject();
					reply.put("from", from);
					reply.put("text", body.optString("content"));

					response.getWriter().println("data: " + reply.toString());
					response.getWriter().println();
					response.getWriter().flush();
					logger.info("======> chatStream [" + reply + "] written");
				}
			} while (poll != null);
		} catch (InterruptedException e) {
			logger.error("getChatStream [" + chatId + " with long poll - interrupted", e);
		}
		logger.info("getChatStream [" + chatId + "] EXIT");
	}

	@Deprecated
	@GetMapping(path = "/test_put")
	public void testPut(@RequestParam String chatId, @RequestParam String message) {
		logger.info("testPut invoked with chatId = " + chatId + ", message = " + message);
		IQueue<String> queue = hazelcast.getQueue("chat|" + chatId);

		JSONObject json = new JSONObject();
		json.put("text", message);

		queue.add(json.toString());
		logger.info("getChatStream EXIT");
	}

}
