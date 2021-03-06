package com.taurus.permanent.normal;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.taurus.permanent.data.ISocketChannel;

public class NormalSocketChannel implements ISocketChannel{
	private final SocketChannel channel;
	
	public NormalSocketChannel(SocketChannel channel) {
		this.channel = channel;
	}
	@Override
	public long write(ByteBuffer buffer) {
		try {
			return channel.write(buffer);
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public void write(String p0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SocketAddress getRemoteAddress() {
		if(checkConnection()) {
			return channel.socket().getRemoteSocketAddress();
		}
		return null;
	}

	@Override
	public SocketAddress getLocalAddress() {
		if(checkConnection()) {
			return channel.socket().getLocalSocketAddress();
		}
		return null;
	}

	@Override
	public void close() throws IOException{
		Socket socket = channel.socket();
		if ((socket != null) && (!socket.isClosed())) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
			channel.close();
		}
	}
	@Override
	public Object getChannel() {
		return channel;
	}
	
	@Override
	public boolean checkConnection() {
		if((channel != null) && (channel.socket() != null) && (!channel.socket().isClosed())) {
			return true;
		}
		return false;
	}

}
