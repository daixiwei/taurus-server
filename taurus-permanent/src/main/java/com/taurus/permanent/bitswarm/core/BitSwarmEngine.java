package com.taurus.permanent.bitswarm.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taurus.core.events.IEvent;
import com.taurus.core.events.IEventListener;
import com.taurus.core.service.IService;
import com.taurus.core.util.Logger;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.bitswarm.data.BindableSocket;
import com.taurus.permanent.bitswarm.data.Packet;
import com.taurus.permanent.bitswarm.io.IOHandler;
import com.taurus.permanent.bitswarm.sessions.SessionManager;
import com.taurus.permanent.core.DefaultConstants;
import com.taurus.permanent.core.TPEvents;
import com.taurus.permanent.core.ServerConfig;
import com.taurus.permanent.core.ServerConfig.SocketAddress;

/**
 * 核心网络字节群处理类
 * @author daixiwei daixiwei15@126.com
 */
public final class BitSwarmEngine extends BaseCoreService {
	private static BitSwarmEngine		__engine__;
	private ISocketAcceptor				socketAcceptor;
	private ISocketReader				socketReader;
	private ISocketWriter				socketWriter;
	
	private Logger						logger;
	private ServerConfig				config;
	private SessionManager				sessionManager;
	private volatile boolean			inited	= false;
	private Map<String, IService>		coreServicesByName;
	private Map<IService, Object>		configByService;
	private IEventListener				eventHandler;
	
	public static BitSwarmEngine getInstance() {
		if (__engine__ == null) {
			__engine__ = new BitSwarmEngine();
		}
		return __engine__;
	}
	
	private BitSwarmEngine() {
		setName("BitSwarmEngine");
	}
	
	private void initializeServerEngine() {
		logger = Logger.getLogger(BitSwarmEngine.class);
		this.config = TaurusPermanent.getInstance().getConfig();
		inited = true;
	}
	

	
	public void start() throws Exception {
		if (!inited) {
			initializeServerEngine();
		}
		logger.info("Start Bit Swarm Engine!");
		
		eventHandler = new IEventListener() {
			public void handleEvent(IEvent event) {
				dispatchEvent(event);
			}
		};
		coreServicesByName = new ConcurrentHashMap<String, IService>();
		configByService = new HashMap<IService, Object>();
		
		bootSequence();
		
		((BaseCoreService) socketReader).addEventListener(TPEvents.SESSION_LOST, this.eventHandler);
	}
	

	private final void bootSequence() throws Exception {
		logger.info("BitSwarmEngine :  { " + Thread.currentThread().getName() + " }");
		
		startCoreServices();
		
		bindSockets(config.socketAddresses);
		for (IService service : coreServicesByName.values()) {
			if (service != null) {
				service.init(configByService.get(service));
			}
		}
	}
	
	
	public void write(Packet response) {
		writeToSocket(response);
	}
	
	private void writeToSocket(Packet res) {
		socketWriter.getIOHandler().onDataWrite(res);
	}
	

	private void startCoreServices() throws Exception {
		sessionManager = SessionManager.getInstance();

		socketReader = new SocketReader(config.socketReaderThreadPoolSize);
		// instance io handler
		IOHandler ioHandler = new IOHandler();
		socketReader.setIoHandler(ioHandler);

		// instance socket acceptor
		socketAcceptor = new SocketAcceptor(config.socketAcceptorThreadPoolSize);
		// instance socket writer
		socketWriter = new SocketWriter(config.socketWriterThreadPoolSize);
		socketWriter.setIOHandler(ioHandler);

		sessionManager.setName(DefaultConstants.SERVICE_SESSION_MANAGER);
		
		((BaseCoreService) socketAcceptor).setName(DefaultConstants.SERVICE_SOCKET_ACCEPTOR);
		((BaseCoreService) socketReader).setName(DefaultConstants.SERVICE_SOCKET_READER);
		((BaseCoreService) socketWriter).setName(DefaultConstants.SERVICE_SOCKET_WRITER);
		
		coreServicesByName.put(DefaultConstants.SERVICE_SESSION_MANAGER, sessionManager);
		
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_ACCEPTOR, (IService) socketAcceptor);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_READER, (IService) socketReader);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_WRITER, (IService) socketWriter);
	}
	
	private void stopCoreServices() throws Exception {
		((IService) socketWriter).destroy(null);
		((IService) socketReader).destroy(null);
		
		Thread.sleep(2000L);
		
		
		sessionManager.destroy(null);
		((IService) socketAcceptor).destroy(null);
	}
	
	private void bindSockets(List<SocketAddress> bindableSockets) {
		for (SocketAddress socketCfg : bindableSockets) {
			try {
				this.socketAcceptor.bindSocket(socketCfg);
			} catch (IOException e) {
				logger.error(e);
				logger.warn("Was not able to bind socket: " + socketCfg);
			}
		}
		
		for (String blockedIp : config.ipFilter.addressBlackList) {
			socketAcceptor.getConnectionFilter().addBannedAddress(blockedIp);
		}

		for (String allowedIp : config.ipFilter.addressWhiteList) {
			socketAcceptor.getConnectionFilter().addWhiteListAddress(allowedIp);
		}

		socketAcceptor.getConnectionFilter().setMaxConnectionsPerIp(config.ipFilter.maxConnectionsPerAddress);

		List<BindableSocket> sockets = socketAcceptor.getBoundSockets();
		String message = "Listening Sockets: ";
		for (BindableSocket socket : sockets) {
			message = message + socket.toString() + " ";
		}
		logger.info(message);
	}
	
	public IService getServiceByName(String serviceName) {
		return coreServicesByName.get(serviceName);
	}
	
	public ISocketAcceptor getSocketAcceptor() {
		return this.socketAcceptor;
	}
	
	public ISocketReader getSocketReader() {
		return this.socketReader;
	}
	
	public ISocketWriter getSocketWriter() {
		return this.socketWriter;
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public ServerConfig getConfig() {
		return this.config;
	}

	
	public SessionManager getSessionManager() {
		return this.sessionManager;
	}
	
	public void init(Object o) {
		throw new UnsupportedOperationException("This call is not supported in this class!");
	}
	
	public void destroy(Object o) {
		try {
			stopCoreServices();
		} catch (Exception e) {
			logger.error("Destroy exception!\n",e);
		}
	}
}
