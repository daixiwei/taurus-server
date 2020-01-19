package com.taurus.permanent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.taurus.core.events.Event;
import com.taurus.core.events.EventManager;
import com.taurus.core.events.IEventListener;
import com.taurus.core.plugin.PluginService;
import com.taurus.core.routes.Extension;
import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;
import com.taurus.core.util.task.TaskScheduler;
import com.taurus.permanent.core.BitSwarmEngine;
import com.taurus.permanent.core.DefaultConstants;
import com.taurus.permanent.core.ServerConfig;
import com.taurus.permanent.core.ServerConfig.ExecutorConfig;
import com.taurus.permanent.core.ServerState;
import com.taurus.permanent.core.SessionManager;
import com.taurus.permanent.core.SystemController;
import com.taurus.permanent.core.TPEvents;
import com.taurus.permanent.data.Session;
import com.taurus.permanent.util.GhostUserHunter;

/**
 * The server main class.
 * 
 * @author daixiwei daixiwei15@126.com
 *
 */
public final class TPServer {
	/**
	 * The server version.
	 */
	private final String				version		= "1.0.1";
	/**
	 * The server class instance.
	 */
	private static TPServer				_instance	= null;
	private final BitSwarmEngine		bitSwarmEngine;
	private final Logger				log;
	private volatile ServerState		state		= ServerState.STARTING;
	private volatile boolean			initialized	= false;
	private volatile long				serverStartTime;
	private ServerConfig				config;
	private IEventListener				networkEvtListener;
	private ScheduledThreadPoolExecutor	timerPool;
	private TaskScheduler				taskScheduler;
	private EventManager				eventManager;
	private GhostUserHunter				ghostUserHunter;
	private SystemController			controller;
	private Extension					extension;
	private ThreadPoolExecutor			systemExecutor;
	private ThreadPoolExecutor			extensionExecutor;

	/**
	 * get main instance
	 */
	public static TPServer me() {
		if (_instance == null) {
			_instance = new TPServer();
		}
		return _instance;
	}

	private TPServer() {
		bitSwarmEngine = BitSwarmEngine.getInstance();

		networkEvtListener = new NetworkEvtListener();
		timerPool = new ScheduledThreadPoolExecutor(1);
		log = Logger.getLogger(getClass());

	}

	public String getVersion() {
		return version;
	}

	private static final ServerConfig loadServerSettings() throws Exception {
		FileInputStream is = new FileInputStream(DefaultConstants.SERVER_CFG_FILE);
		ServerConfig config = new ServerConfig();
		config.load(is);
		return config;
	}

	public void start() {
		System.out.println("\n==============================================================================\n" + 
							">>Begin start taurus-permanent server....\n"
							+ "============================================================================== \n");
		if (!initialized) {
			initialize();
		}

		try {

			PluginService.me().loadConfig();
			log.info("Load taurus-core config finish");

			this.config = loadServerSettings();
			initExecutors();

			this.taskScheduler = new TaskScheduler();
			this.taskScheduler.init(null);

			this.eventManager = new EventManager(systemExecutor);
			eventManager.init(null);

			timerPool.setCorePoolSize(config.timerThreadPoolSize);
			bitSwarmEngine.init(null);

			log.info("\n\n==============================================================================\n" + 
						">>Init Extension...\n"+ 
						"============================================================================== \n");
			controller = new SystemController();
			ghostUserHunter = new GhostUserHunter();
			extension = instanceExtension();
			controller.init(null);
			extension.onStart();

			state = ServerState.STARTED;
			log.info("\n\n==============================================================================\n" + 
						">>Server(" + version + ") ready!\n"
					+ "============================================================================== \n");

			serverStartTime = System.currentTimeMillis();
		} catch (FileNotFoundException e) {
			log.error("Not find taurus-core.xml and taurus-permanent.xml", e);
		} catch (Exception e) {
			log.error("Server start exception!", e);
		}
	}

	private void initExecutors() {
		final ExecutorConfig sys_cfg = this.config.systemThreadPoolConfig;
		this.systemExecutor = new ThreadPoolExecutor(sys_cfg.corePoolSize, sys_cfg.maxPoolSize, sys_cfg.keepAliveTime, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(sys_cfg.maxQueueSize), new TPThreadFactory(sys_cfg.name));

		final ExecutorConfig ext_cfg = this.config.extensionThreadPoolConfig;
		this.extensionExecutor = new ThreadPoolExecutor(ext_cfg.corePoolSize, ext_cfg.maxPoolSize, ext_cfg.keepAliveTime, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(ext_cfg.maxQueueSize), new TPThreadFactory(ext_cfg.name));
	}

	/**
	 * shut down server.
	 */
	public void shutdown() {
		try {
			log.info("Server shutdown!");
			List<?> awaitingExecution = timerPool.shutdownNow();
			log.info("stopping timer pool: " + awaitingExecution.size());

			bitSwarmEngine.destroy(null);
			eventManager.destroy(null);
			this.controller.destroy(null);
			extension.onStop();
		} catch (Exception e) {
			log.error("shut down exception!", e);
		}
	}

	public ScheduledThreadPoolExecutor getTimerPool() {
		return timerPool;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public ThreadPoolExecutor getSystemExecutor() {
		return systemExecutor;
	}

	public ThreadPoolExecutor getExtensionExecutor() {
		return extensionExecutor;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public SessionManager getSessionManager() {
		return bitSwarmEngine.getSessionManager();
	}

	public SystemController getController() {
		return controller;
	}

	public Extension getExtension() {
		return extension;
	}

	public ServerState getState() {
		return state;
	}

	public ServerConfig getConfig() {
		return config;
	}

	/**
	 * 获取服务器启动时间
	 * 
	 * @return
	 */
	public long getUptime() {
		if (serverStartTime == 0L) {
			throw new IllegalStateException("Server not ready yet, cannot provide uptime!");
		}
		return System.currentTimeMillis() - serverStartTime;
	}

	private void initialize() {
		if (initialized) {
			throw new IllegalStateException("SmartFoxServer engine already initialized!");
		}
		bitSwarmEngine.addEventListener(TPEvents.SESSION_LOST, networkEvtListener);
		bitSwarmEngine.addEventListener(TPEvents.SESSION_IDLE, networkEvtListener);
		bitSwarmEngine.addEventListener(TPEvents.SESSION_IDLE_CHECK_COMPLETE, networkEvtListener);

		initialized = true;
	}

	private Extension instanceExtension() {
		ServerConfig.ExtensionConfig extensionConfig = config.extensionConfig;
		if (StringUtil.isEmpty(extensionConfig.className)) {
			throw new RuntimeException("Extension className parameter is missing!");
		}
		if (StringUtil.isEmpty(extensionConfig.name)) {
			throw new RuntimeException("Extension name parameter is missing!");
		}
		Extension extension = null;
		try {
			Class<?> extensionClass = Class.forName(extensionConfig.className);
			if (!Extension.class.isAssignableFrom(extensionClass)) {
				throw new RuntimeException("Extension does not extends Extension: " + extensionConfig.name);
			}
			extension = (Extension) extensionClass.newInstance();
			extension.setName(extensionConfig.name);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Illegal access while instantiating class: " + extensionConfig.className);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot instantiate class: " + extensionConfig.className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found: " + extensionConfig.className);
		}
		return extension;
	}

	private void onSessionClosed(Session session) {
		controller.disconnect(session);
	}

	private void onSessionIdle(Session idleSession) {
		controller.disconnect(idleSession);
	}

	/**
	 * session 网络事件监听
	 */
	private class NetworkEvtListener implements IEventListener {
		private NetworkEvtListener() {
		}

		public void handleEvent(Event event) {
			String evtName = event.getName();

			if (evtName.equals(TPEvents.SESSION_LOST)) {
				Session session = (Session) event.getParameter(TPEvents.PARAM_SESSION);

				if (session == null) {
					throw new RuntimeException("session is null!");
				}
				onSessionClosed(session);
			} else if ((evtName.equals(TPEvents.SESSION_IDLE_CHECK_COMPLETE))) {
				ghostUserHunter.hunt();
			} else if (evtName.equals(TPEvents.SESSION_IDLE)) {
				onSessionIdle((Session) event.getParameter(TPEvents.PARAM_SESSION));
			}
		}
	}

	private static final class TPThreadFactory implements ThreadFactory {
		private static final AtomicInteger	POOL_ID;
		private static final String			THREAD_BASE_NAME	= "%s:%s";
		private final AtomicInteger			threadId;
		private final String				poolName;

		static {
			POOL_ID = new AtomicInteger(0);
		}

		public TPThreadFactory(final String poolName) {
			this.threadId = new AtomicInteger(1);
			this.poolName = poolName;
			TPThreadFactory.POOL_ID.incrementAndGet();
		}

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(r,
					String.format(THREAD_BASE_NAME, (this.poolName != null) ? this.poolName : TPThreadFactory.POOL_ID.get(), this.threadId.getAndIncrement()));
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != 5) {
				t.setPriority(5);
			}
			return t;
		}
	}
}
