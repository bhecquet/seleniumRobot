package com.seleniumtests.connectors.mails;

import java.util.Calendar;

public class Appointment {
	private String reason;
	private Calendar datetime;
	
	public Appointment(String reason, Calendar datetime) {
		this.reason = reason;
		this.datetime = datetime;
	}

	public String getReason() {
		return reason;
	}

	public Calendar getDatetime() {
		return datetime;
	}
}
