package it.olly.teamsconnectorsample.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiFilter implements Filter {
	public static final String URL_PATTERNS = "/api/*";

	private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		logger.info("AuthFilter [" + URL_PATTERNS + "] - " + req.getRequestURI());
		chain.doFilter(request, response);
	}

}
