package com.taurus.permanent.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.core.events.Event;
import com.taurus.core.events.EventManager;
import com.taurus.core.service.IService;
import com.taurus.core.util.Logger;
import com.taurus.core.util.MD5;
import com.taurus.core.util.Utils;
import com.taurus.core.util.executor.TaurusExecutor;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.data.Packet;
import com.taurus.permanent.data.Session;

/**
 * 核心控制器基类
 * 
 * @author daixiwei daixiwei15@126.com
 */
public class SystemController implements IService {
	public static final String		CONNECT_TOKE				= "$t";
	public static final String		CONNECT_PROT_COMPRESSION	= "$pc";
	public static final String		REQUEST_CMD					= "$c";
	public static final String		REQUEST_GID					= "$gi";
	public static final String		REQUEST_PARM				= "$p";
	public static final String		REQUEST_RESULT				= "$r";

	/**
	 * 连接验证
	 */
	public static final int			ACTION_HANDSHAKE	= 0;
	/**
	 * pingpong
	 */
	public static final int			ACTION_PINGPONG		= 1;
	/**
	 * 客户端请求
	 */
	public static final int			ACTION_REQUST_CMD	= 2;
	/**
	 * 服务器事件消息
	 */
	public static final int			ACTION_EVENT_CMD	= 3;

	private volatile boolean		active;
	private String					name				= "SystemController";
	private TaurusExecutor			threadPool;

	private final Logger			logger;
	private final TaurusPermanent	taurus;

	private SessionManager			sessionManager;
	private ActionMapping			actionMapping;
	private final Routes routes;
	
	public SystemController() {
		logger = Logger.getLogger(SystemController.class);
		taurus = TaurusPermanent.getInstance();
		sessionManager = taurus.getSessionManager();
		routes = new Routes() {public void config() {}};
	}

	public void init(Object o) {
		if (active) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		threadPool = taurus.getExtensionExecutor();

		// EventManager eventManager = taurus.getEventManager();
		// eventManager.addEventListener(TPEvents.EVENT_SESSION_DISCONNECT, this);
		taurus.getExtension().configRoute(routes);
		
		actionMapping = new ActionMapping(routes);
		actionMapping.buildActionMapping();
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

	private final void processRequest(Packet packet) throws Exception {
		Session sender = packet.getSender();
		if (sender.isIdle() || sender.isMarkedForEviction())
			return;
		if (!sessionManager.containsSession(sender)) {
			logger.warn(" session is already expired!");
			return;
		}

		byte reqId = (byte) packet.getId();

		switch (reqId) {
		case ACTION_HANDSHAKE:
			onHandshake(sender);
			break;
		case ACTION_PINGPONG:
			onPingPong(sender);
			break;
		case ACTION_REQUST_CMD:
			if (sender.getHashId() == null)
				return;
			onRequest(sender, packet);
			break;
		}
	}

	private final void onPingPong(Session sender) {
		Packet packet = new Packet();
		packet.setId(ACTION_PINGPONG);
		packet.setRecipient(sender);
		packet.setData(new TObject());
		BitSwarmEngine.getInstance().write(packet);
	}

	private static String __getUniqueSessionToken(final Session session) {
		final String key = String.valueOf(session.getFullIpAddress()) + "__" + String.valueOf(Utils.rand.nextInt());
		return MD5.getInstance().getHash(key);
	}

	private final void onHandshake(Session sender) {
		ITObject parm = new TObject();
		String token = __getUniqueSessionToken(sender);
		parm.putString(CONNECT_TOKE, __getUniqueSessionToken(sender));
		parm.putInt(CONNECT_PROT_COMPRESSION, taurus.getConfig().protocolCompression);
		sender.setHashId(token);
		sender.updateLastActivityTime();
		Packet packet = new Packet();
		packet.setId(ACTION_HANDSHAKE);
		packet.setRecipient(sender);
		packet.setData(parm);
		BitSwarmEngine.getInstance().write(packet);
	}

	private final void onRequest(Session sender, Packet packet) throws Exception{
		ITObject parm = (ITObject) packet.getData();
		String key = parm.getString(REQUEST_CMD);
		Action action = actionMapping.getAction(key);

		if (action == null) {
			return;
		}

		Controller controller = action.getController();
		int gid = 0;
		if (parm.containsKey(REQUEST_GID))
			gid = parm.getInt(REQUEST_GID);
		ITObject p = null;
		if (parm.containsKey(REQUEST_PARM)) {
			p = parm.getTObject(REQUEST_PARM);
		}
		TRequest tqp = new TRequest(key,sender, gid,p);
		action.getMethod().invoke(controller,tqp);
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
			if (session.getHashId() != null) {
				Event evt = new Event(TPEvents.EVENT_SESSION_DISCONNECT);
				evt.setParameter(TPEvents.PARAM_SESSION, session);
				taurus.getEventManager().dispatchEvent(evt);
				session.setHashId(null);
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
	 * @param actionKey 事件协议号
	 * @param params 数据参数
	 * @param recipient 客户端session
	 */
	public void sendEvent(String actionKey, ITObject params, Session recipient) {
		List<Session> msgRecipients = new ArrayList<Session>();
		msgRecipients.add(recipient);
		sendEvent(actionKey, params, msgRecipients);
	}

	/**
	 * 发送事件给客户端
	 * 
	 * @param actionKey 事件协议号
	 * @param params 数据参数
	 * @param recipients 客户端session列表
	 */
	public void sendEvent(String actionKey, ITObject params, List<Session> recipients) {
		ITObject resObj = TObject.newInstance();
		resObj.putString(REQUEST_CMD, actionKey);
		if (params != null) {
			resObj.putString(REQUEST_PARM, params.toJson());
		}
		Packet packet = new Packet();
		packet.setId(ACTION_EVENT_CMD);
		packet.setData(resObj);
		packet.setRecipients(recipients);
		BitSwarmEngine.getInstance().write(packet);
	}

	public final Logger getLogger() {
		return logger;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isActive() {
		return active;
	}

}
