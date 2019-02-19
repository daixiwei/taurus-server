package com.taurus.web;

public abstract class Extension {
	private String name;
	
	/**
	 * Config route
	 */
	public abstract void configRoute(Routes me);
	
	/**
	 * Call back server start
	 */
	public void onStart() {}
	
	/**
	 * Call back server stop
	 */
	public void onStop() {}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
