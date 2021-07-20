package it.olly.teamsconnectorsample.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/msnotification")
public class MSNotificationValidator {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PostMapping(path = "", produces = "text/plain")
	public void receiveNotification(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("receiveNotification Called");
		printRequest(request);
		String validationToken = request.getParameter("validationToken");
		if (validationToken != null) {
			logger.info("receiveNotification - got validation request");
			response.getWriter().print(validationToken);
		} else {
			logger.info("receiveNotification - got notification");
		}
	}

	private void printRequest(HttpServletRequest httpRequest) throws IOException {
		System.out.println("*** Headers");

		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			System.out.println(headerName + " = " + httpRequest.getHeader(headerName));
		}

		System.out.println("\n*** Parameters");

		Enumeration<String> params = httpRequest.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = (String) params.nextElement();
			System.out.println(paramName + " = " + httpRequest.getParameter(paramName));
		}

		System.out.println("\n*** Row data");
		String body = httpRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		System.out.println(body);

	}
}
