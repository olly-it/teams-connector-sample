package it.olly.teamsconnectorsample.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

@RestController
@RequestMapping("/")
public class PageHTMLController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// CONFIG STUFF
	@Value("${ms.client_id}")
	public String CLIENT_ID;
	@Value("${ms.client_secret}")
	public String CLIENT_SECRET;
	@Value("${ms.redirect_uri}")
	public String REDIRECT_URI;
	@Value("${ms.scope}")
	public String SCOPE;

	@Autowired
	private MSClientHelper msClientHelper;

	// useful?
	public static final String STATE = "12345";

	@GetMapping(produces = MediaType.TEXT_HTML_VALUE)
	public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("index Called");
		response.getWriter().println("<html><body>");
		response.getWriter().println("HI BOY!<br>");
		String loginUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize" //
				+ "?client_id=" + CLIENT_ID //
				+ "&response_type=code" //
				+ "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, Charset.forName("utf-8")) //
				+ "&response_mode=query" //
				+ "&scope=" + URLEncoder.encode(SCOPE, Charset.forName("utf-8")) //
				+ "&state=" + STATE;
		response.getWriter().println("<a href=\"" + loginUrl + "\">login</a>");
		response.getWriter().println("</body></html>");
	}

	@GetMapping(path = "/loggedIn", produces = MediaType.TEXT_HTML_VALUE)
	public void loggedIn(@RequestParam String accessToken, HttpServletResponse response) throws IOException {
		response.getWriter().println("<html><body>");
		response.getWriter().println("<br><br>ACCESS_TOKEN:<br>");
		response.getWriter().println(accessToken);
		response.getWriter().println("<br><br>CHATS:<BR>");

		/*
		 * List<Chat> chats = msClientHelper.getChats(accessToken); for (Chat chat :
		 * chats) { response.getWriter() .println(chat.id + " [" + chat.topic + "] #" +
		 * chat.chatType + " - " + chat.members + "<BR>"); }
		 */
		JSONObject json = msClientHelper.lowLevelGet("/beta/me/chats", accessToken);
		JSONArray chats = json.getJSONArray("value");
		response.getWriter().println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
				+ "<th>id</th>" //
				+ "<th>topic</th>" //
				+ "<th>lastUpdatedDateTime</th>" //
				+ "<th>chatType</th>" //
				+ "</tr>");
		for (int i = 0; i < chats.length(); i++) {
			JSONObject chatJO = chats.getJSONObject(i);
			String id = chatJO.getString("id");
			String topic = chatJO.optString("topic");
			String lastUpdatedDateTime = chatJO.optString("lastUpdatedDateTime");
			String chatType = chatJO.optString("chatType");
			String href = "/inChat?accessToken=" + accessToken + "&chatId=" + id;
			response.getWriter().println("<tr>" //
					+ "<td><a href=\"" + href + "\">" + id + "</a></td>" //
					+ "<td>" + topic + "</td>" //
					+ "<td>" + lastUpdatedDateTime + "</td>" //
					+ "<td>" + chatType + "</td>" //
					+ "</tr>");
		}
		response.getWriter().println("</tr></table><br>");

		response.getWriter().println("<br><br>CHANNELS:<BR>");
		// TODO get all channels

		/*
		 * response.getWriter().
		 * println("<form action='/api/messages/chatmessages' method='get'>");
		 * response.getWriter().
		 * println("<input type='hidden' name='accessToken' value='" + accessToken +
		 * "'>"); response.getWriter().println(
		 * "chatId = <input type='text' name='chatId' value='19:206d344c-f515-4da3-9f89-af764f71a50d_8c714f55-36bd-4d6c-b505-c2b130edeadd@unq.gbl.spaces'>"
		 * );
		 * response.getWriter().println("<input type='submit' value='chatmessages'>");
		 * response.getWriter().println("</form>");
		 */
		response.getWriter().println("</body></html>");
	}

	@GetMapping(path = "/inChat", produces = MediaType.TEXT_HTML_VALUE)
	public void inChat(@RequestParam String accessToken, @RequestParam String chatId, HttpServletResponse response)
			throws IOException {
		response.getWriter().println("<html><body>");
		response.getWriter().println("<b>" + (new Date()) + " - webhook exp. 10'</b><br>");
		JSONObject json = msClientHelper.lowLevelGet("/beta/me/chats/" + chatId + "/messages", accessToken);
		JSONArray messages = json.getJSONArray("value");

		// send part
		response.getWriter().println(""//
				+ "<script>\n" //
				+ "function sendMsg() {\n"//
				+ "	var msg_text = document.getElementById('msg_text');\n"//
				+ "    var url = \"/api/chats/send?accessToken=" + accessToken + "&chatId=" + chatId
				+ "&message=\"+encodeURIComponent(msg_text.value);\n"//
				+ "    msg_text.value=\"\";\n"//
				+ "    var xmlHttp = new XMLHttpRequest();\n"//
				+ "    xmlHttp.open(\"GET\", url, true);\n"//
				+ "    xmlHttp.send(null);\n"//
				+ "}\n" + "</script>\n");
		response.getWriter().println("<input type=\"text\" id=\"msg_text\" size=\"20\" name=\"message\" value=\"\"/>");
		response.getWriter().println("<input type=\"button\" value=\"send\" onclick=\"sendMsg();\"/>");
		response.getWriter().println("<hr>");

		// realtime stuff
		String streamUrl = "/api/stream/chat?chatId=" + chatId + "&accessToken=" + accessToken;
		response.getWriter().println("" //
				+ "<script>" //
				+ "        const evtSource = new EventSource('" + streamUrl + "', { withCredentials: false } );\n" //
				+ "        evtSource.onmessage = function(event) {\n" //
				+ "            console.log('got event', event);\n" //
				+ "            var p = document.createElement('p');\n" //
				+ "            var json = JSON.parse(event.data);\n" //
				+ "            p.innerHTML = json.from+\" - \"+json.text;\n" //
				+ "            document.getElementById('realtime').appendChild(p);\n" //
				+ "        }\n" // s
				+ "</script>");
		response.getWriter().println("<div id='realtime'></div>");
		// subscribe to webhook TODO check if not already subscribed + manage expiration
		String webhookResource = "/chats/" + chatId + "/messages";
		try {
			if (msClientHelper.alreadySubscribedToWebhook(webhookResource, accessToken)) {
				logger.info("already subscribed to webhook");
				response.getWriter()
						.println("<br><b>already subscribed to webhook - will i receive notifications?</b>");
			} else {
				msClientHelper.subscribeToWebhook(webhookResource, accessToken);
				response.getWriter().println("<br><b>subscribed to webhook: " + webhookResource + "</b>");
			}
		} catch (Exception e) {
			logger.warn("already subscribed?", e);
			response.getWriter().println("<br><b>can't subscribe to webhook: " + e.getMessage() + "</b>");
		}
		response.getWriter().println("<br><hr>");

		// message list
		response.getWriter().println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
				+ "<th>id</th>" //
				+ "<th>createdDateTime</th>" //
				+ "<th>from</th>" //
				+ "<th>body</th>" //
				+ "</tr>");
		for (int i = 0; i < messages.length(); i++) {
			JSONObject msgJO = messages.getJSONObject(i);
			String id = msgJO.getString("id");
			String createdDateTime = msgJO.getString("createdDateTime");
			// sometimes from is null
			JSONObject fromJO = msgJO.optJSONObject("from");
			String from = fromJO != null ? fromJO.getJSONObject("user").getString("displayName") : "[empty]";
			JSONObject body = msgJO.getJSONObject("body");

			response.getWriter().println("<tr>" //
					+ "<td>" + id + "</td>" //
					+ "<td>" + createdDateTime + "</td>" //
					+ "<td>" + from + "</td>" //
					+ "<td>" + body.optString("content") + "</td>" //
					+ "</tr>");
		}
		response.getWriter().println("</table>");

		response.getWriter().println("</body></html>");
	}
}
