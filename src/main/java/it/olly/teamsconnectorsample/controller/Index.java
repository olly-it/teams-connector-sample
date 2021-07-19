package it.olly.teamsconnectorsample.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/", "/index" })
public class Index {
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

	public static final String STATE = "12345";

	@GetMapping(produces = MediaType.TEXT_HTML_VALUE)
	public void receiveAuthorizeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
}
