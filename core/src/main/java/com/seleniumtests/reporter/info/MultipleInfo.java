package com.seleniumtests.reporter.info;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow to specify multiple date for the same piece of information
 * @author S047432
 *
 */
public class MultipleInfo extends StringInfo {

	private List<StringInfo> infos;
	
	public MultipleInfo(String info) {
		super(info);
		infos = new ArrayList<>();
	}
	
	public void addInfo(StringInfo info) {
		infos.add(info);
	}
	
	@Override
	public String encode(String format) {
		StringBuilder out = new StringBuilder();
		for (StringInfo info: infos) {
			out.append(info.encode(format));
		}
		return out.toString();
	}
	
	

}
