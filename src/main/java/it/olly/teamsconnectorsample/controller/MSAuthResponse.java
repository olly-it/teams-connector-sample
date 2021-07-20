package it.olly.teamsconnectorsample.controller;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/msauthresponse")
public class MSAuthResponse {
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

	// public static final String REDIRECT_URI =
	// "http://localhost:8080/msauthresponse";
	// public static final String SCOPE = "https://graph.microsoft.com/mail.read";

	@Autowired
	private RestTemplate restTemplate;

	public static class MSResponseForAccessToken {
		public String token_type; // " Bearer",
		public String scope; // "openid%20offline_access%&20https://graph.microsoft.com/mail.read",
		public Integer expires_in; // 3600,
		public String access_token; // "eyJ0eXAiOiJKV1Qi...",
		public String refresh_token; // "AwABAAAAvPM1KaPl..."
	}

	/**
	 * <ol>
	 * <li>request an access at
	 * https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=0580a610-...&response_type=code&redirect_uri=http://localhost:8080/msauthresponse&response_mode=query&scope=openid%20offline_access%20https%3A%2F%2Fgraph.microsoft.com%2Fmail.read&state=12345
	 * </li>
	 * <li>redirect url will be called with a "code" in querystring</li>
	 * <li>exchange the code to the accessToken with POST
	 * https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token with parameters:
	 * client_id=6731de76-...&scope=https%3A%2F%2Fgraph.microsoft.com%2Fmail.read&code=...</li>
	 * <li>use the token</li>
	 * </ol>
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@GetMapping()
	public void receiveAuthorizeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("receiveAuthorizeResponse Called");
		response.setContentType("text/html");
		response.getWriter().println("<html><body>");
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			logger.info(name + ": " + request.getParameter(name));
			response.getWriter().println(name + ": " + request.getParameter(name) + "<br>");
		}

		// exchange code to accessToken
		logger.info("exchange code to accessToken");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", CLIENT_ID);
		map.add("scope", SCOPE);
		map.add("code", request.getParameter("code"));
		map.add("redirect_uri", REDIRECT_URI);
		map.add("grant_type", "authorization_code");
		map.add("client_secret", CLIENT_SECRET);

		String postUri = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<MSResponseForAccessToken> tokenObj = restTemplate.exchange(postUri, HttpMethod.POST, entity,
					MSResponseForAccessToken.class);

			logger.info("GOT RESPONSE TOKEN: " + tokenObj);
			response.getWriter().println("<br><br>ACCESS_TOKEN: <br>");
			response.getWriter().println(tokenObj.getBody().access_token);
			response.getWriter().println("<br><br>REFRESH_TOKEN: <br>");
			response.getWriter().println(tokenObj.getBody().refresh_token);
			response.getWriter().println("<br><br>");
			response.getWriter().println("<a href=\"/api/messages?accessToken=" + tokenObj.getBody().access_token
					+ "\">read messages</a><br>");
			response.getWriter().println("<br><a href=\"/api/messages/chats?accessToken="
					+ tokenObj.getBody().access_token + "\">list chats</a><br>");
			response.getWriter().println("<br>");
			response.getWriter().println("<form action='/api/messages/chatmessages' method='get'>");
			response.getWriter().println(
					"<input type='hidden' name='accessToken' value='" + tokenObj.getBody().access_token + "'>");
			response.getWriter().println(
					"chatId = <input type='text' name='chatId' value='19:206d344c-f515-4da3-9f89-af764f71a50d_8c714f55-36bd-4d6c-b505-c2b130edeadd@unq.gbl.spaces'>");
			response.getWriter().println("<input type='submit' value='chatmessages'>");
			response.getWriter().println("</form>");
		} catch (Exception e) {
			response.getWriter().println("<b>" + e.getMessage() + "</b>");
			response.getWriter().println("<pre>");
			e.printStackTrace(response.getWriter());
			response.getWriter().println("</pre>");
		}
		response.getWriter().println("</body></html>");
		response.flushBuffer();
	}

}
