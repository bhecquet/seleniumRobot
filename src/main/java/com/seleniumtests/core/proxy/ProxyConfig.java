package com.seleniumtests.core.proxy;

import org.openqa.selenium.Proxy.ProxyType;

public class ProxyConfig {

	private ProxyType type;
	private String address;
	private Integer port;
	private String login;
	private String password;
	private String exclude;
	private String pac;
	public ProxyType getType() {
		return type;
	}
	public void setType(ProxyType type) {
		this.type = type;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getExclude() {
		return exclude;
	}
	public void setExclude(String exclude) {
		this.exclude = exclude;
	}
	public String getPac() {
		return pac;
	}
	public void setPac(String pac) {
		this.pac = pac;
	}
	public String getAddressAndPort() {
		return String.format("%s:%s", address, port);
	}
	
	
}
