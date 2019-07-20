package com.taurus;

import com.taurus.core.routes.ActionKey;
import com.taurus.permanent.core.Controller;

public class T1Controller extends Controller{

	@ActionKey("test")
	public void test() {
		System.out.println("t1 test");
		this.sendResponse(0, this.getParams());
	}
}
