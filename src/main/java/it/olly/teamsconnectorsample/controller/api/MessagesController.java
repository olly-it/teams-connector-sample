package it.olly.teamsconnectorsample.controller.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/stream")
public class MessagesController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@GetMapping(path = "/chat")
	public void getChatStream(@RequestParam String chatId, HttpServletResponse response) throws IOException {
		response.setContentType("text/event-stream");
		response.setCharacterEncoding("utf-8");
		logger.info("getChatStream invoked with chatId = " + chatId);
		for (int i = 0; i < 10; i++) {
			logger.info("writing msg");
			String msg = "{\"text\":\"ciao come stai?\"}";
			response.getWriter().println("data: " + msg);
			response.getWriter().println();
			response.getWriter().flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("getChatStream EXIT");
	}

}
