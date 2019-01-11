package com.taurus.permanent.webscoket;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.taurus.core.util.Logger;
import com.taurus.core.util.executor.TaurusExecutor;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.core.BaseCoreService;
import com.taurus.permanent.core.BitSwarmEngine;
import com.taurus.permanent.data.IPacketQueue;
import com.taurus.permanent.data.ISocketChannel;
import com.taurus.permanent.data.PackDataType;
import com.taurus.permanent.data.Packet;
import com.taurus.permanent.data.Session;
import com.taurus.permanent.data.SessionManager;
import com.taurus.permanent.data.SessionType;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class WebSocketService extends BaseCoreService{
	private Undertow server;
	private final BitSwarmEngine	engine;
	private SessionManager sessionManager;
	private TaurusExecutor systemExecutor;
	private final BlockingQueue<Session>	sessionTicketsQueue;
	private Logger logger;
	
	public WebSocketService() {
		engine = BitSwarmEngine.getInstance();
		sessionManager = engine.getSessionManager();
		systemExecutor = TaurusPermanent.getInstance().getSystemExecutor();
		sessionTicketsQueue = new LinkedBlockingQueue<Session>();
		logger = Logger.getLogger(WebSocketService.class);
	}
	
	public void init(Object o) {
		super.init(o);
		WSConnectionListener listener = new WSConnectionListener(this);
		server = Undertow.builder().addHttpListener(8080, "0.0.0.0")
				.setHandler(Handlers.path().addPrefixPath("/websocket", Handlers.websocket(listener)))
				.build();
		server.start();
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		server.stop();
		server = null;
	}
	
	private void openAction(WebSocketChannel channel) {
		Session session = sessionManager.createSession(new UndertowWebSocketChannel(channel), SessionType.WEBSOCKET);
		sessionManager.addSession(session);
	}
	
	private void readTextAction(WebSocketChannel channel,String data) {
		Session session = sessionManager.getSessionByConnection(channel);
		Packet newPacket = new Packet();
		newPacket.setDataType(PackDataType.TEXT);
		newPacket.setSender(session);
		newPacket.setData(data);
		systemExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				engine.getProtocolHandler().onPacketRead(newPacket);
			}
		});
	}
	
	/**
	 * send packet
	 */
	public void onDataWrite(Packet packet) {
		if (packet.getRecipients().size() > 0) {
			packet.setDataType(PackDataType.TEXT);
			engine.getProtocolHandler().onPacketWrite(packet);
			for (final Session session : packet.getRecipients()) {
	            final ISocketChannel channel = session.getConnection();
	            try {
					channel.write((String)packet.getData());
				} catch (IOException e) {
					logger.error("Websocket write data exception!",e);
					continue;
				}
	        }
		}
	}
	
	private void closeAction(WebSocketChannel channel) {
		try {
			sessionManager.onSocketDisconnected(channel);
		} catch (IOException e) {
			throw new RuntimeException("WebSocket Disconnected exception!",e);
		}
	}
	
	private static final class WSConnectionListener implements WebSocketConnectionCallback{
		private WSListener listener;
		private WebSocketService wsService;
		
		public WSConnectionListener(WebSocketService wsService) {
			this.wsService = wsService;
			this.listener = new WSListener(wsService);
		}
		
		@Override
		public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
			channel.getReceiveSetter().set(this.listener);
			channel.resumeReceives();
			wsService.openAction(channel);
		}
		
	}

	private static final class WSListener extends AbstractReceiveListener{
		private WebSocketService wsService;
		
		public WSListener(WebSocketService wsService) {
			this.wsService = wsService;
		}
		
		@Override
		protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
			wsService.readTextAction(channel, message.getData());
//			WebSockets.sendText(message.getData(), channel, null);
		}
		
		protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
			int code = cm.getCode();
			System.out.println("code:"+code);
	        wsService.closeAction(channel);
	    }

	    protected void onFullPingMessage(final WebSocketChannel channel, BufferedBinaryMessage message)  throws IOException {
	    	super.onFullBinaryMessage(channel, message);
	    }

	    protected void onFullBinaryMessage(final WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
	    	super.onFullBinaryMessage(channel, message);
	    }
		
	}
	
	
}
