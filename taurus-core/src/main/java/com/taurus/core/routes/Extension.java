package com.taurus.core.routes;

/**
 * Extension class.
 * @author daixiwei
 *
 */
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
	
	/**
	 * set extension name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * get extension name
	 * @return
	 */
	public String getName() {
		return this.name;
	}
}
