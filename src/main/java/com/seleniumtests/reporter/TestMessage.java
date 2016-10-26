package com.seleniumtests.reporter;

public class TestMessage extends TestAction {
	
	private MessageType messageType;


	public enum MessageType {
		ERROR,
		WARNING,	// warning message
		INFO,		// success message
		LOG			// neutral message
	}
	
	public TestMessage(String name, MessageType type) {
		super(name, type == MessageType.ERROR ? true: false);
		messageType = type;
	}

	public MessageType getMessageType() {
		return messageType;
	}
}
