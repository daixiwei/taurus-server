package com.taurus;

import com.taurus.permanent.core.ActionKey;
import com.taurus.permanent.core.Controller;
import com.taurus.permanent.core.TRequest;

public class T1Controller extends Controller{

	@ActionKey("test")
	public void test(TRequest request) {
		System.out.println("t1 test");
		request.sendResponse(0, request.getParm());
	}
}
