package com.taurus.permanent.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.core.util.Logger;
import com.taurus.core.util.Utils;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.core.BaseCoreService;
import com.taurus.permanent.core.BitSwarmEngine;
import com.taurus.permanent.core.ServerConfig;
import com.taurus.permanent.core.SessionManager;
import com.taurus.permanent.data.ISocketChannel;
import com.taurus.permanent.data.PackDataType;
import com.taurus.permanent.data.Packet;
import com.taurus.permanent.data.Session;
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

/**
 * WebSocket service
 * @author daixiwei
 *
 */
public class WebSocketService extends BaseCoreService{
	private Undertow server;
	private final BitSwarmEngine	engine;
	private SessionManager sessionManager;
	private Logger logger;
	
	public WebSocketService() {
		engine = BitSwarmEngine.getInstance();
		sessionManager = engine.getSessionManager();
		logger = Logger.getLogger(WebSocketService.class);
	}
	
	public void init(Object o) {
		super.init(o);
		WSConnectionListener listener = new WSConnectionListener(this);
		ServerConfig config = TaurusPermanent.getInstance().getConfig();
		server = Undertow.builder().addHttpListener(config.webSocketConfig.port, config.webSocketConfig.address)
				.setHandler(Handlers.path().addPrefixPath("/websocket", Handlers.websocket(listener)))
				.build();
		server.start();
		logger.info("Websocket listen --> "+config.webSocketConfig.address+":"+config.webSocketConfig.port);
		logger.info("Websocket service start!");
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		server.stop();
		server = null;
		logger.info("Websocket service shutdown!");
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
		ITObject requestObject = TObject.newFromJsonData(data);
		newPacket.setData(requestObject);
		session.setLastReadTime(System.currentTimeMillis());
		engine.getProtocolHandler().onPacketRead(newPacket);	
	}
	
	private void readBinaryAction(WebSocketChannel channel,ByteBuffer data) {
		Session session = sessionManager.getSessionByConnection(channel);
		Packet newPacket = new Packet();
		newPacket.setDataType(PackDataType.BINARY);
		boolean compressed = data.get() >0;
		byte[] bytes = new byte[data.remaining()];
		data.get(bytes);
		if(compressed) {
			try {
				bytes = Utils.uncompress(bytes);
			} catch (IOException e) {
				logger.error(e);
				return;
			}
		}
		newPacket.setSender(session);
		ITObject requestObject = TObject.newFromBinaryData(bytes);
		newPacket.setData(requestObject);
		session.setLastReadTime(System.currentTimeMillis());
		engine.getProtocolHandler().onPacketRead(newPacket);	
	}
	
	
	/**
	 * send packet
	 */
	public void onDataWrite(Packet packet){
		if (packet.getRecipients().size() > 0) {
			packet.setDataType(PackDataType.BINARY);
			engine.getProtocolHandler().onPacketWrite(packet);
			int protocolCompressionThreshold = TaurusPermanent.getInstance().getConfig().protocolCompression;
			byte[] binData = ((TObject)packet.getData()).toBinary();
			boolean compression = binData.length > protocolCompressionThreshold;
			if(compression) {
				try {
					binData = Utils.compress(binData);
				} catch (IOException e) {
					logger.error(e);
					return;
				}
			}
			ByteBuffer writeBuffer = ByteBuffer.allocate(1 + binData.length);
			writeBuffer.put(compression?(byte)1:(byte)0);
			writeBuffer.put(binData);
			writeBuffer.flip();
			for (final Session session : packet.getRecipients()) {
				session.setLastWriteTime(System.currentTimeMillis());
	            final ISocketChannel channel = session.getConnection();
	            WebSockets.sendBinary(writeBuffer, (WebSocketChannel)channel.getChannel(), null);
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
		}


		protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
	        wsService.closeAction(channel);
	    }

	    protected void onFullPingMessage(final WebSocketChannel channel, BufferedBinaryMessage message)  throws IOException {
	    	super.onFullBinaryMessage(channel, message);
	    }

	    protected void onFullBinaryMessage(final WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
	    	ByteBuffer[] bufferList= message.getData().getResource();
	    	for (ByteBuffer tem : bufferList) {
	    		wsService.readBinaryAction(channel, tem);
			}
	    	message.getData().free();
	    }
		
	}
	
	
}
