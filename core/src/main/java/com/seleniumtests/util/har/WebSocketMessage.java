package com.seleniumtests.util.har;

public class WebSocketMessage {
	
	private String type;
	private double time;
	private int opcode;
	private String data;
	
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
