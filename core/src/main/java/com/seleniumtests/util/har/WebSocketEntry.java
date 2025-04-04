package com.seleniumtests.util.har;

import java.util.List;

public class WebSocketEntry extends Entry {
	
	private List<WebSocketMessage> webSocketMessages;
	
	public WebSocketEntry(String pageref, String startedDateTime, Request request, Response response, Timing timings, int time) {
		super(pageref, startedDateTime, request, response, timings, time);
	}
	
	public WebSocketEntry(String pageref, String startedDateTime, Request request, Response response, Timing timings, int time, List<WebSocketMessage> webSocketMessages) {
		super(pageref, startedDateTime, request, response, timings, time);
		this.webSocketMessages = webSocketMessages;
	}
	
	public List<WebSocketMessage> getWebSocketMessages() {
		return webSocketMessages;
	}
	
}
