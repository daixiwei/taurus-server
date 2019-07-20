package com.taurus;

import com.taurus.core.routes.ActionKey;
import com.taurus.permanent.core.Controller;

public class T2Controller extends Controller{
	
	@ActionKey("test")
	public void test() {
		System.out.println("t2 test");
		this.sendResponse(1, this.getParams());
	}
	
	@ActionKey("test1")
	public void test1() {
		System.out.println("t2 test");
		this.sendResponse(1, this.getParams());
	}
}
