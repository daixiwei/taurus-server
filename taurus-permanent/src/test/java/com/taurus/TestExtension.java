package com.taurus;

import com.taurus.core.entity.ITObject;
import com.taurus.core.events.Event;
import com.taurus.core.routes.Extension;
import com.taurus.core.routes.Routes;
import com.taurus.permanent.core.TPEvents;
import com.taurus.permanent.data.Session;

/**   
 * @author daixiwei daixiwei15@126.com 
 * @date 2016年12月21日 下午12:42:33 
 * @version V2.9   
 */
public class TestExtension extends Extension{
	
	public TestExtension() {
		super();
//		login_cmd_map.put("req_test", true);
	}

	@Override
	public void configRoute(Routes me) {
		me.add("t1", T1Controller.class);
		me.add("t2", T2Controller.class);
	}
	
//	@Override
//	public void handleEvent(Event event) {
//		if( event.getName() == TPEvents.EVENT_SESSION_DISCONNECT){
//			Session session =  (Session) event.getParameter(TPEvents.PARAM_SESSION);
//			logger.info("session:" + session.isIdle());
//		}
//		
//	}
//
//	@Override
//	protected void handlerRequest(Session sender, String cmdName, ITObject params, int gid) {
//		// TODO Auto-generated method stub
//		params.putInt("ttt", 1);
//		sendResponse(gid, 0, params, sender);
//		
//		//api.disconnect(sender);
//	}


	
}
