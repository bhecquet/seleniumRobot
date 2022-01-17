package com.seleniumtests.reporter.info;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.Sys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Allow to specify multiple date for the same piece of information
 * @author S047432
 *
 */
public class MultipleInfo extends Info {



	private List<Info> infos;
	
	public MultipleInfo(String info) {
		super(info);
		infos = new ArrayList<>();
	}
	
	public void addInfo(Info info) {
		infos.add(info);
	}
	
	@Override
	public String encode(String format) {
		StringBuilder out = new StringBuilder();
		for (Info info: infos) {
			try {
				out.append(info.encode(format));
			} catch (NullPointerException | AssertionError message) {
				logger.error("format cannot be null");
				return "";
			}
		}
		return out.toString();
	}

	public List<Info> getInfos() {
		return infos;
	}

}
