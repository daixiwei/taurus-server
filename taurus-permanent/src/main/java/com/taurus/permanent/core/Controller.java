package com.taurus.permanent.core;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.permanent.data.Packet;
import com.taurus.permanent.data.Session;

/**
 * Controller
 * @author daixiwei
 *
 */
public abstract class Controller {
	private String actionKey;
	private Session session;
	private int gid;
	private ITObject param;
	private volatile boolean isFinish;
	
	
	void _init(String actionKey,Session sender,int gid,ITObject param){
		this.actionKey = actionKey;
		this.session = sender;
		this.gid = gid;
		this.param = param;
	}
	
	/**
	 * get action key.
	 * @return
	 */
	public String getActionKey() {
		return actionKey;
	}
	
	/**
	 * 获取session对象
	 * @return
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * 获取参数
	 * @return
	 */
	public ITObject getParams() {
		return param;
	}
	
	/**
	 * 动态响应客户端请示
	 * @param result 响应结果 0成功
	 * @param params 数据参数
	 */
	public void sendResponse(int result, ITObject params) {
		if(gid==0)return;
		if(isFinish) {
			throw new RuntimeException("This response is finish!");
		}
		isFinish = true;
		if(!session.isConnected())return;
		ITObject resObj = TObject.newInstance();
		resObj.putInt(SystemController.REQUEST_RESULT, result);
		resObj.putInt(SystemController.REQUEST_GID, gid);
		if (params != null) {
			resObj.putTObject(SystemController.REQUEST_PARM, params);
		}
		Packet packet = new Packet();
		packet.setId(SystemController.ACTION_REQUST_CMD);
		packet.setData(resObj);
		packet.setRecipient(session);
		BitSwarmEngine.getInstance().write(packet);
	}
}
