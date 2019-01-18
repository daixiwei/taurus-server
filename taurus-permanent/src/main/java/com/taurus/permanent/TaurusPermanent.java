package com.taurus.permanent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.taurus.core.events.Event;
import com.taurus.core.events.EventManager;
import com.taurus.core.events.IEventListener;
import com.taurus.core.plugin.PluginService;
import com.taurus.core.util.Logger;
import com.taurus.core.util.executor.ExecutorConfig;
import com.taurus.core.util.executor.TaurusExecutor;
import com.taurus.core.util.task.TaskScheduler;
import com.taurus.permanent.core.BitSwarmEngine;
import com.taurus.permanent.core.DefaultConstants;
import com.taurus.permanent.core.Extension;
import com.taurus.permanent.core.ServerConfig;
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
public final class TaurusPermanent {
	/**
	 * The server version.
	 */
	private final String				version		= "1.0.0";
	/**
	 * The server class instance.
	 */
	private static TaurusPermanent		_instance	= null;
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
	private TaurusExecutor				systemExecutor;
	private TaurusExecutor				extensionExecutor;

	/**
	 * get main instance
	 */
	public static TaurusPermanent getInstance() {
		if (_instance == null) {
			_instance = new TaurusPermanent();
		}
		return _instance;
	}

	private TaurusPermanent() {
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

			PluginService.getInstance().loadConfig();
			log.info("Load taurus-core config finish");

			this.config = loadServerSettings();
			initExecutors();

			this.taskScheduler = new TaskScheduler();
			this.taskScheduler.init(null);

			this.eventManager = new EventManager(extensionExecutor);
			eventManager.init(null);

			timerPool.setCorePoolSize(config.timerThreadPoolSize);
			bitSwarmEngine.init(null);

			log.info("\n\n==============================================================================\n" + 
					">>Init Extension...\n"
					+ "============================================================================== \n");
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
		sys_cfg.name = "Sys";
		this.systemExecutor = new TaurusExecutor(sys_cfg);

		final ExecutorConfig ext_cfg = this.config.extensionThreadPoolConfig;
		ext_cfg.name = "Ext";
		this.extensionExecutor = new TaurusExecutor(ext_cfg);
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

	public TaurusExecutor getSystemExecutor() {
		return systemExecutor;
	}

	public TaurusExecutor getExtensionExecutor() {
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
		if ((extensionConfig.className == null) || (extensionConfig.className.length() == 0)) {
			throw new RuntimeException("Extension file parameter is missing!");
		}
		if ((extensionConfig.name == null) || (extensionConfig.name.length() == 0)) {
			throw new RuntimeException("Extension name parameter is missing!");
		}
		Extension extension = null;
		try {
			Class<?> extensionClass = Class.forName(extensionConfig.className);
			if (!Extension.class.isAssignableFrom(extensionClass)) {
				throw new RuntimeException("Controller does not implement IController interface: " + extensionConfig.name);
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
}
