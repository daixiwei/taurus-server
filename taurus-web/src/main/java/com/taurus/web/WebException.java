package com.taurus.web;
 
public class WebException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	
	public WebException(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
