package com.taurus;

import com.taurus.core.entity.TObject;
import com.taurus.core.routes.ActionKey;
import com.taurus.core.routes.IController;
import com.taurus.permanent.TPServer;
import com.taurus.permanent.data.Session;

public class T1Controller implements IController{

	@ActionKey("test")
	public void test(Session sender,TObject params,int gid) {
		System.out.println("t1 test");
		TPServer.me().getController().sendResponse(gid, 0, params, sender);
	}
}
