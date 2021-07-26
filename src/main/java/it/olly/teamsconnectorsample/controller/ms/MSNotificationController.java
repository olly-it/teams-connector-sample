package it.olly.teamsconnectorsample.controller.ms;

import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;

@RestController
@RequestMapping("/msnotification")
public class MSNotificationController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HazelcastInstance hazelcast;

	@PostMapping(path = "", produces = "text/plain")
	public void receiveNotification(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("receiveNotification Called");
		String body = parseRequest(request);
		String validationToken = request.getParameter("validationToken");
		if (validationToken != null) {
			logger.info("receiveNotification - got validation request");
			response.getWriter().print(validationToken);
		} else {
			logger.info("receiveNotification - got notification");
			JSONObject json = new JSONObject(body);
			JSONArray jsonArray = json.getJSONArray("value");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jj = jsonArray.getJSONObject(i);
				String res = jj.getString("resource");

				// TODO parse it in a better way! here i do very strictly assumptions
				// resource is like:
				// chats('19:206d344c-f515-4...q.gbl.spaces')/messages('1627312266605')
				if (res.startsWith("chats('")) {
					// it's a chat
					res = res.substring(7);
					String chatId = res.substring(0, res.indexOf("')"));
					res = res.substring(res.indexOf("')") + 2);
					if (res.startsWith("/messages('")) {
						// it's a message
						res = res.substring(11);
						String messageId = res.substring(0, res.indexOf("')"));
						JSONObject chatMsgJson = new JSONObject();
						chatMsgJson.put("messageId", messageId);
						hazelcast.getQueue("chat|" + chatId).add(chatMsgJson.toString());
					}
				}
			}
		}
	}

	private String parseRequest(HttpServletRequest httpRequest) throws IOException {
		logger.debug("*** Headers");

		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			logger.debug(headerName + " = " + httpRequest.getHeader(headerName));
		}

		logger.debug("\n*** Parameters");

		Enumeration<String> params = httpRequest.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = (String) params.nextElement();
			logger.debug(paramName + " = " + httpRequest.getParameter(paramName));
		}

		logger.debug("\n*** Row data");
		String body = httpRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		logger.debug(body);
		return body;
	}
}
