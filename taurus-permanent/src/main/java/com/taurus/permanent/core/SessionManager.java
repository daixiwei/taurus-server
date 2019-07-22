package com.taurus.permanent.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.taurus.core.events.Event;
import com.taurus.core.service.AbstractService;
import com.taurus.core.util.Logger;
import com.taurus.core.util.task.ITaskHandler;
import com.taurus.core.util.task.Task;
import com.taurus.core.util.task.TaskScheduler;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.data.IPacketQueue;
import com.taurus.permanent.data.ISocketChannel;
import com.taurus.permanent.data.NonBlockingPacketQueue;
import com.taurus.permanent.data.Session;
import com.taurus.permanent.data.SessionType;

/**
 * session管理器，负责创建，添加和删除session
 * @author daixiwei daixiwei15@126.com
 */
public final class SessionManager extends AbstractService {
	private static final String							SESSION_CLEANING_TASK_ID			= "SessionCleanerTask";
	private static final int							SESSION_CLEANING_INTERVAL_SECONDS	= 10;
	private static SessionManager						__instance__;
	private Logger										logger;
	private final ConcurrentMap<Integer, Session>		sessionsById;
	private BitSwarmEngine								engine								= null;
	private final List<Session>							sessionList;
	private final ConcurrentMap<Object, Session>		sessionsByConnection;
	private Task										sessionCleanTask;
	private TaskScheduler								systemScheduler;
	private int											highestCCS							= 0;

	public static SessionManager getInstance() {
		if (__instance__ == null) {
			__instance__ = new SessionManager();
		}
		return __instance__;
	}

	private SessionManager() {
		sessionsById = new ConcurrentHashMap<Integer, Session>();
		sessionList = new ArrayList<Session>();
		sessionsByConnection = new ConcurrentHashMap<Object, Session>();
	}

	public void init(Object o) {
		name = "DefaultSessionManager";
		engine = BitSwarmEngine.getInstance();
		logger = Logger.getLogger(SessionManager.class);
		
		systemScheduler = TaurusPermanent.getInstance().getTaskScheduler();
		sessionCleanTask = new Task(SESSION_CLEANING_TASK_ID);
		systemScheduler.addScheduledTask(sessionCleanTask, SESSION_CLEANING_INTERVAL_SECONDS, true, new SessionCleaner());

		active = true;
		logger.info("session manager init!");
	}

	public void destroy(Object o) {
		super.destroy(o);
		sessionCleanTask.setActive(false);
		shutDownLocalSessions();
		sessionsByConnection.clear();
	}

	/**
	 * 添加session对象
	 * @param session
	 */
	public void addSession(Session session) {
		synchronized (sessionList) {
			sessionList.add(session);
		}
		sessionsById.put(session.getId(), session);
		sessionsByConnection.put(session.getConnection().getChannel(), session);
		

		if (sessionList.size() > highestCCS) {
			highestCCS = sessionList.size();
		}
//		logger.info("Session created: " + session);
	}

	/**
	 * 检查session是否存在
	 * @param session
	 * @return
	 */
	public boolean containsSession(Session session) {
		return sessionsById.containsKey(session.getId());
	}

	/**
	 * 移除指定session对象
	 * @param session
	 */
	public void removeSession(Session session) {
		if (session == null)return;
		if(!sessionsById.containsKey(session.getId()))return;
		synchronized (sessionList) {
			sessionList.remove(session);
		}
		ISocketChannel connection = session.getConnection();
		sessionsById.remove(session.getId());
		if (connection != null) {
			sessionsByConnection.remove(connection.getChannel());
		}
		if ((session.getType() == SessionType.NORMAL) || (session.getType() == SessionType.WEBSOCKET)) {
			engine.getConnectionFilter().removeAddress(session.getAddress());
		}

//		logger.info("Session removed: " + session);
	}

	/**
	 * 移除指定ID的session
	 * @param id
	 * @return
	 */
	public Session removeSession(int id) {
		Session session = sessionsById.get(id);
		if (session != null) {
			removeSession(session);
		}
		return session;
	}

	/**
	 * 移除指定channel的session
	 * @param connection
	 * @return
	 */
	public Session removeSession(Object connection) {
		Session session = getSessionByConnection(connection);
		if (session != null) {
			removeSession(session);
		}
		return session;
	}

	/**
	 * channel链接断开处理
	 * @param connection
	 * @throws IOException
	 */
	public void onSocketDisconnected(Object connection) throws IOException {
		Session session = sessionsByConnection.get(connection);
		if (session == null) {
			return;
		}
		sessionsByConnection.remove(connection);
		session.setConnected(false);
		removeSession(session);
		dispatchLostSessionEvent(session);
	}

	/**
	 * 获取服务器所有session
	 * @return
	 */
	public List<Session> getAllSessions() {
		List<Session> allSessions = null;

		synchronized (sessionList) {
			allSessions = new ArrayList<Session>(sessionList);
		}

		return allSessions;
	}

	/**
	 * 获取指定channel的session
	 * @param connection
	 * @return
	 */
	public Session getSessionByConnection(Object connection) {
		return sessionsByConnection.get(connection);
	}

	/**
	 * 获取指定ID的session
	 * @param id
	 * @return
	 */
	public Session getSessionById(int id) {
		return sessionsById.get(Integer.valueOf(id));
	}

	/**
	 * 获取最高的流量数量
	 * @return
	 */
	public int getHighestCCS() {
		return highestCCS;
	}

	/**
	 * 关闭本地所有session
	 */
	public void shutDownLocalSessions() {
		synchronized (sessionList) {
			for (Iterator<Session> it = sessionList.iterator(); it.hasNext();) {
				Session session = (Session) it.next();
				it.remove();
				try {
					session.close();
				} catch (IOException e) {
					logger.warn("I/O Error while closing session: " + session);
				}
			}
		}
	}

	/**
	 * 创建 session
	 * @param channel
	 * @return
	 */
	public Session createSession(ISocketChannel channel,SessionType type) {
		Session session = new Session();
		session.setConnection(channel);
		session.setTimeout(engine.getConfig().sessionTimeout);
		session.setType(type);
		IPacketQueue packetQueue = new NonBlockingPacketQueue(engine.getConfig().sessionPacketQueueSize);
		session.setPacketQueue(packetQueue);
		return session;
	}

	/**
	 * 获取当前session链接数
	 * @return
	 */
	public int getSessionCount() {
		return sessionList.size();
	}

	private void applySessionCleaning() {
		if (getSessionCount() > 0) {
			for (Session session : getAllSessions()) {
				if ((session == null) || (session.isFrozen())) {
					continue;
				}
				if (session.isMarkedForEviction()) {
					terminateSession(session);
					logger.info("Terminated idle logged-in session: " + session);
				} else {
					if (!session.isIdle()) {
						continue;
					}
					if (session.getHashId()!=null) {
						logger.info("session timeout:" + session);

						session.setMarkedForEviction();
						dispatchSessionIdleEvent(session);
					} else {
						terminateSession(session);
					}
				}
			}
		}

		Event event = new Event(TPEvents.SESSION_IDLE_CHECK_COMPLETE);
		engine.dispatchEvent(event);
	}

	private void terminateSession(Session session) {
		if (session.getType() == SessionType.NORMAL) {
			ISocketChannel connection = session.getConnection();

			try {
				connection.close();
				session.setConnected(false);
			} catch (Exception err) {
				this.logger.warn("Failed closing connection while removing idle Session: " + session);
			}
		}

		removeSession(session);
		dispatchLostSessionEvent(session);
	}

	private void dispatchLostSessionEvent(Session closedSession) {
		Event event = new Event(TPEvents.SESSION_LOST);
		event.setParameter(TPEvents.PARAM_SESSION, closedSession);
		engine.dispatchEvent(event);
	}

	private void dispatchSessionIdleEvent(Session idleSession) {
		Event event = new Event(TPEvents.SESSION_IDLE);
		event.setParameter(TPEvents.PARAM_SESSION, idleSession);
		engine.dispatchEvent(event);
	}

	/**
	 * 清理session 任务
	 */
	private final class SessionCleaner implements ITaskHandler {
		private SessionCleaner() {
		}

		public void doTask(Task task) throws Exception {
			applySessionCleaning();
		}
	}

}
