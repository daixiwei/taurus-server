package com.taurus.permanent.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.core.events.Event;
import com.taurus.core.events.EventManager;
import com.taurus.core.events.IEventListener;
import com.taurus.core.util.Logger;
import com.taurus.core.util.Utils;
import com.taurus.core.util.executor.TaurusExecutor;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.bitswarm.core.BitSwarmEngine;
import com.taurus.permanent.bitswarm.data.Packet;
import com.taurus.permanent.bitswarm.sessions.Session;

/**
 * 核心控制器基类
 * 
 * @author daixiwei daixiwei15@126.com
 */
public abstract class IController implements IEventListener {

	public static final String		CONNECT_TOKE		= "$t";
	public static final String		REQUEST_CMD			= "$c";
	public static final String		REQUEST_GID			= "$gi";
	public static final String		REQUEST_PARM		= "$p";
	public static final String		REQUEST_RESULT		= "$r";

	public static final int			ACTION_PINGPONG		= 0;
	public static final int			ACTION_REQUST_CMD	= 1;
	public static final int			ACTION_EVENT_CMD	= 2;

	protected volatile boolean		active;
	protected TaurusExecutor		threadPool;

	protected final Logger			logger;
	protected final TaurusPermanent	taurus;

	protected Map<String, Boolean>	login_cmd_map;

	public IController() {
		logger = Logger.getLogger(getClass());
		login_cmd_map = new HashMap<String, Boolean>();
		taurus = TaurusPermanent.getInstance();
	}

	public void init(Object o) {
		if (active) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		threadPool = taurus.getExtensionExecutor();

		EventManager eventManager = taurus.getEventManager();
		eventManager.addEventListener(TPEvents.EVENT_SESSION_DISCONNECT, this);
		active = true;
	}

	public void destroy(Object o) {
		active = false;
		List<?> leftOvers = threadPool.shutdownNow();
		EventManager eventManager = taurus.getEventManager();
		eventManager.removeAllListener();
		logger.info("Controller stopping: " + getClass().getName() + ", Unprocessed tasks: " + leftOvers.size());
	}

	public void enqueueRequest(Packet request) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (active) {
					try {
						processRequest(request);
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
		});
	}

	private final void processRequest(Packet request) throws Exception {
		Session sender = request.getSender();
		if (sender.isIdle() || sender.isMarkedForEviction())
			return;

		byte reqId = (byte) request.getId();
		ITObject parm = (ITObject) request.getData();
		if (reqId == ACTION_PINGPONG) {
			onPingPong(sender);
		} else if (reqId == ACTION_REQUST_CMD) {
			String cmdName = parm.getString(REQUEST_CMD);
			if (!login_cmd_map.containsKey(cmdName)) {
				if (!sender.isLoggedIn()) {
					logger.warn("cmd[" + cmdName + "]not logged:" + sender);
					return;
				}
			} else {
				if (!taurus.getSessionManager().containsSession(sender)) {
					logger.warn(" session is already expired!");
					return;
				}
			}
			int guid = 0;
			if (parm.containsKey(REQUEST_GID))
				guid = parm.getInt(REQUEST_GID);
			ITObject p = null;
			if (parm.containsKey(REQUEST_PARM)) {
				byte[] bytes = parm.getByteArray(REQUEST_PARM);
				p = Utils.bytesToJson(bytes);
			}
			handlerRequest(sender, cmdName, p, guid);
		}

		if (sender.isLoggedIn()) {
			sender.setMaxLoggedInIdleTime(taurus.getConfig().userMaxIdleTime);
			sender.setLastLoggedInActivityTime(System.currentTimeMillis());
		}
	}

	private final void onPingPong(Session sender) {
		Packet packet = new Packet();
		packet.setId(ACTION_PINGPONG);
		packet.setRecipient(sender);
		packet.setData(new TObject());
		BitSwarmEngine.getInstance().write(packet);
	}

	/**
	 * 断开session
	 * 
	 * @param session
	 */
	public void disconnect(Session session) {
		if (session == null) {
			throw new RuntimeException("Session object is null.");
		}

		try {
			if (session.isLoggedIn()) {
				session.setLoggedIn(false);
				Event evt = new Event(TPEvents.EVENT_SESSION_DISCONNECT);
				evt.setParameter(TPEvents.PARAM_SESSION, session);
				taurus.getEventManager().dispatchEvent(evt);
			}
			if (session.isConnected()) {
				session.close();
			}
		} catch (IOException err) {
			throw new RuntimeException(err);
		}
	}

	/**
	 * 发送事件给单一客户端
	 * 
	 * @param cmdName 事件协议号
	 * @param params 数据参数
	 * @param recipient 客户端session
	 */
	public void sendEvent(String cmdName, ITObject params, Session recipient) {
		List<Session> msgRecipients = new ArrayList<Session>();
		msgRecipients.add(recipient);
		sendEvent(cmdName, params, msgRecipients);
	}

	/**
	 * 发送事件给客户端
	 * 
	 * @param cmdName 事件协议号
	 * @param params 数据参数
	 * @param recipients 客户端session列表
	 */
	public void sendEvent(String cmdName, ITObject params, List<Session> recipients) {
		byte[] bytes = null;
		try {
			bytes = Utils.jsonToBytes(params);
		} catch (IOException e) {
			logger.error(e);
		}

		ITObject resObj = TObject.newInstance();
		resObj.putString(REQUEST_CMD, cmdName);
		if (bytes != null) {
			resObj.putByteArray(REQUEST_PARM, bytes);
		}

		Packet packet = new Packet();
		packet.setId(IController.ACTION_EVENT_CMD);
		packet.setData(resObj);
		packet.setRecipients(recipients);
		BitSwarmEngine.getInstance().write(packet);
	}

	/**
	 * 动态响应客户端请示
	 * 
	 * @param gid 响应标识ID
	 * @param result 响应结果 0成功
	 * @param params 数据参数
	 * @param recipient 客户端session
	 */
	public void sendResponse(int gid, int result, ITObject params, Session recipient) {
		List<Session> msgRecipients = new ArrayList<Session>();
		msgRecipients.add(recipient);
		sendResponse(gid, result, params, msgRecipients);
	}

	/**
	 * 动态响应客户端请示
	 * 
	 * @param gid 响应标识ID
	 * @param result 响应结果 0成功
	 * @param params 数据参数
	 * @param recipients 客户端session列表
	 */
	public void sendResponse(int gid, int result, ITObject params, List<Session> recipients) {
		byte[] bytes = null;
		try {
			bytes = Utils.jsonToBytes(params);
		} catch (IOException e) {
			logger.error(e);
		}
		ITObject resObj = TObject.newInstance();
		resObj.putInt(REQUEST_RESULT, result);
		resObj.putInt(REQUEST_GID, gid);
		if (bytes != null) {
			resObj.putByteArray(REQUEST_PARM, bytes);
		}
		Packet packet = new Packet();
		packet.setId(IController.ACTION_REQUST_CMD);
		packet.setData(resObj);
		packet.setRecipients(recipients);
		BitSwarmEngine.getInstance().write(packet);
	}

	protected abstract void handlerRequest(Session sender, String cmdName, ITObject params, int gid);

	public final Logger getLogger() {
		return logger;
	}

}
