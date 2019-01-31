package com.taurus;

import com.taurus.permanent.core.ActionKey;
import com.taurus.permanent.core.Controller;

public class T2Controller extends Controller{
	
	@ActionKey("test")
	public void test() {
		System.out.println("t2 test");
		this.sendResponse(1, this.getParm());
	}
}
