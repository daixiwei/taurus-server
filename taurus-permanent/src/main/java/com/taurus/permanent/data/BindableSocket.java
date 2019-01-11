package com.taurus.permanent.data;

import java.nio.channels.SelectableChannel;

/**
 * BindableSocket
 * @author daixiwei daixiwei15@126.com
 */
public class BindableSocket {
	protected SelectableChannel	channel;
	private String				address;
	private int					port;
	
	public BindableSocket(SelectableChannel channel, String address, int port) {
		this.address = address;
		this.port = port;
		
		this.channel = channel;
	}
	
	public SelectableChannel getChannel() {
		return channel;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	

	public String toString() {
		return String.format("%s[%d]", address, port);
	}
}
