package com.taurus.permanent.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taurus.core.events.Event;
import com.taurus.core.events.IEventListener;
import com.taurus.core.service.IService;
import com.taurus.core.util.Logger;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.core.ServerConfig.SocketAddress;
import com.taurus.permanent.data.BindableSocket;
import com.taurus.permanent.data.Packet;
import com.taurus.permanent.data.Session;
import com.taurus.permanent.data.SessionType;
import com.taurus.permanent.io.IOHandler;
import com.taurus.permanent.io.ProtocolHandler;
import com.taurus.permanent.normal.SocketAcceptor;
import com.taurus.permanent.normal.SocketReader;
import com.taurus.permanent.normal.SocketWriter;
import com.taurus.permanent.webscoket.WebSocketService;

/**
 * 核心网络字节群处理类
 * @author daixiwei daixiwei15@126.com
 */
public final class BitSwarmEngine extends BaseCoreService {
	private static BitSwarmEngine		__engine__;
	private SocketAcceptor				socketAcceptor;
	private SocketReader				socketReader;
	private SocketWriter				socketWriter;
	
	private Logger						logger;
	private ServerConfig				config;
	private SessionManager				sessionManager;
	private volatile boolean			inited	= false;
	private Map<String, IService>		coreServicesByName;
	private Map<IService, Object>		configByService;
	private IEventListener				eventHandler;
	private WebSocketService			webSocketService;
	private ProtocolHandler				protocolHandler;
	private ConnectionFilter			connectionFilter;
	
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
	
	private final void setConnectionFilterConfig() {
		for (String blockedIp : config.ipFilter.addressBlackList) {
			this.connectionFilter.addBannedAddress(blockedIp);
		}

		for (String allowedIp : config.ipFilter.addressWhiteList) {
			this.connectionFilter.addWhiteListAddress(allowedIp);
		}

		this.connectionFilter.setMaxConnectionsPerIp(config.ipFilter.maxConnectionsPerAddress);
	}
	
	
	public void write(Packet response) {
		try {
			if (this.config.webSocketConfig.isActive) {
	            final List<Session> webSocketRecipients = new ArrayList<Session>();
	            final List<Session> socketRecipients = new ArrayList<Session>();
	            for (final Session session : response.getRecipients()) {
	                if (session.getType() == SessionType.WEBSOCKET) {
	                    webSocketRecipients.add(session);
	                }
	                else {
	                    socketRecipients.add(session);
	                }
	            }
	            if (webSocketRecipients.size() > 0) {
	                response.setRecipients(socketRecipients);
	                final Packet webSocketResponse = response.clone();
	                webSocketResponse.setRecipients(webSocketRecipients);
	                this.writeToWebSocket(webSocketResponse);
	            }
	        }
		}finally {
			writeToSocket(response);
		}
	}
	
	private void writeToSocket(Packet res) {
		socketWriter.getIOHandler().onDataWrite(res);
	}
	
	private void writeToWebSocket(Packet res) {
		webSocketService.onDataWrite(res);
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
		
		if(config.webSocketConfig.isActive) {
			webSocketService = new WebSocketService();
			webSocketService.setName(DefaultConstants.SERVICE_WEB_SOCKET);
			coreServicesByName.put(DefaultConstants.SERVICE_WEB_SOCKET, webSocketService);
		}
		
		
		
		socketAcceptor.setName(DefaultConstants.SERVICE_SOCKET_ACCEPTOR);
		socketReader.setName(DefaultConstants.SERVICE_SOCKET_READER);
		socketWriter.setName(DefaultConstants.SERVICE_SOCKET_WRITER);
		
		
		coreServicesByName.put(DefaultConstants.SERVICE_SESSION_MANAGER, sessionManager);
		
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_ACCEPTOR, socketAcceptor);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_READER, socketReader);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_WRITER, socketWriter);
		
	}
	
	private void stopCoreServices() throws Exception {
		socketWriter.destroy(null);
		socketReader.destroy(null);
		if(webSocketService!=null) {
			webSocketService.destroy(null);
		}
		Thread.sleep(2000L);

		sessionManager.destroy(null);
		socketAcceptor.destroy(null);
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
	
	public SocketAcceptor getSocketAcceptor() {
		return this.socketAcceptor;
	}
	
	public SocketReader getSocketReader() {
		return this.socketReader;
	}
	
	public SocketWriter getSocketWriter() {
		return this.socketWriter;
	}
	
	public ProtocolHandler getProtocolHandler() {
		return this.protocolHandler;
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

	public ConnectionFilter getConnectionFilter() {
		return connectionFilter;
	}
	
	public SessionManager getSessionManager() {
		return this.sessionManager;
	}
	
	public void init(Object o) {
		if (!inited) {
			initializeServerEngine();
		}
		logger.info("Start Bit Swarm Engine!");
		
		eventHandler = new IEventListener() {
			public void handleEvent(Event event) {
				dispatchEvent(event);
			}
		};
		
		protocolHandler = new ProtocolHandler();
		
		connectionFilter = new ConnectionFilter();
		setConnectionFilterConfig();
		
		coreServicesByName = new ConcurrentHashMap<String, IService>();
		configByService = new HashMap<IService, Object>();
		
		try {
			bootSequence();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		socketReader.addEventListener(TPEvents.SESSION_LOST, this.eventHandler);
	}
	
	public void destroy(Object o) {
		try {
			stopCoreServices();
		} catch (Exception e) {
			logger.error("Destroy exception!\n",e);
		}
	}
}
