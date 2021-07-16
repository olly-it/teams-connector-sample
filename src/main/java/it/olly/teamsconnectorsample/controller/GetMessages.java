package it.olly.teamsconnectorsample.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping({ "/getmessages", "/api/messages" }) // TODO leave just /api/getmessages (filtered)
public class GetMessages {
	private static final Logger logger = LoggerFactory.getLogger(GetMessages.class);

	public class Message {
		public String msg;

		public Message(String msg) {
			this.msg = msg;
		}

	}

	@GetMapping()
	public List<Message> get(@RequestParam String accessToken) throws IOException {
		logger.info("get messages invoked with token = " + accessToken);
		List<Message> ret = new ArrayList<>();
		ret.add(new Message("ciao"));
		ret.add(new Message("gigi"));
		return ret;
	}
}
