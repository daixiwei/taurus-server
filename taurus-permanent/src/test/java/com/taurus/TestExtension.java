package com.taurus;

import com.taurus.core.entity.ITObject;
import com.taurus.core.events.IEvent;
import com.taurus.permanent.bitswarm.sessions.Session;
import com.taurus.permanent.core.IController;
import com.taurus.permanent.core.TPEvents;

/**   
 * @author daixiwei daixiwei15@126.com 
 * @date 2016年12月21日 下午12:42:33 
 * @version V2.9   
 */
public class TestExtension extends IController{
	
	public TestExtension() {
		super();
		login_cmd_map.put("req_test", true);
	}
	
	@Override
	public void handleEvent(IEvent event) {
		if( event.getName() == TPEvents.EVENT_SESSION_DISCONNECT){
			Session session =  (Session) event.getParameter(TPEvents.PARAM_SESSION);
			logger.info("session:" + session.isIdle());
		}
		
	}

	@Override
	protected void handlerRequest(Session sender, String cmdName, ITObject params, int gid) {
		// TODO Auto-generated method stub
		params.putInt("ttt", 1);
		sendResponse(gid, 0, params, sender);
		
		//api.disconnect(sender);
	}


	
}