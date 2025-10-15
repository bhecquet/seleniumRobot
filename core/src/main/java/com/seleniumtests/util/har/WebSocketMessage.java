package com.seleniumtests.util.har;

public class WebSocketMessage {
	
	private final String type;
	private final double time;
	private final int opcode;
	private final String data;
	
	public WebSocketMessage(String type, double time, int opcode, String data) {
		this.type = type;
		this.time = time;
		this.opcode = opcode;
		this.data = data;
	}
	
	public String getType() {
		return type;
	}
	
	public double getTime() {
		return time;
	}
	
	public int getOpcode() {
		return opcode;
	}
	
	public String getData() {
		return data;
	}
}
