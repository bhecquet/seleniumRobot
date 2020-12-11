package com.seleniumtests.connectors.tms.reportportal;

import java.util.Calendar;
import java.util.function.Supplier;

import org.testng.util.Strings;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

public class ReportPortalService extends TestNGService {

	private static Launch rpLaunch;

	public ReportPortalService() {
		super(getLaunchOverriddenProperties());
	}
	
	public Launch getLaunch() {
		return ReportPortalService.rpLaunch;
	}


	private static Supplier<Launch> getLaunchOverriddenProperties() {
		ListenerParameters parameters = new ListenerParameters(PropertiesLoader.load());
		parameters.setCallbackReportingEnabled(true);
		ReportPortal reportPortal = ReportPortal.builder().withParameters(parameters).build();
		StartLaunchRQ rq = buildStartLaunch(reportPortal.getParameters());
		
		rpLaunch = reportPortal.newLaunch(rq);
		
		
		return new Supplier<Launch>() {
			@Override
			public Launch get() {
				return rpLaunch;
			}
		};
	}

	private static StartLaunchRQ buildStartLaunch(ListenerParameters parameters) {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setName(parameters.getLaunchName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setAttributes(parameters.getAttributes());
		rq.setMode(parameters.getLaunchRunningMode());
		if (!Strings.isNullOrEmpty(parameters.getDescription())) {
			rq.setDescription(parameters.getDescription());
		}

		return rq;
	}
}
