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

@RestController
@CrossOrigin
@RequestMapping("/api/stream")
public class StreamController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HazelcastInstance hazelcast;

	@GetMapping(path = "/chat")
	public void getChatStream(@RequestParam String chatId, HttpServletResponse response) throws IOException {
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
					response.getWriter().println("data: " + poll);
					response.getWriter().println();
					response.getWriter().flush();
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
