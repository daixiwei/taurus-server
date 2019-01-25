package com.taurus.permanent.websocket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.taurus.permanent.data.ISocketChannel;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

public class UndertowWebSocketChannel implements ISocketChannel{

	private WebSocketChannel channel;
	
	public UndertowWebSocketChannel (WebSocketChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public long write(ByteBuffer buffer) {
		return 0;
	}

	@Override
	public void write(String message) {
		WebSockets.sendText(message, channel, null);
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return channel.getPeerAddress();
	}

	@Override
	public SocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	@Override
	public void close() throws IOException{
		channel.close();
	}

	@Override
	public Object getChannel() {
		return channel;
	}

	@Override
	public boolean checkConnection() {
		if(channel!=null&&channel.isOpen())return true;
		return false;
	}

}
