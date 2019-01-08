package com.taurus.permanent.bitswarm.core;

import com.taurus.permanent.bitswarm.data.BindableSocket;
import com.taurus.permanent.core.ServerConfig.SocketAddress;

import java.io.IOException;
import java.util.List;

/**
 * ISocketAcceptor
 * @author daixiwei daixiwei15@126.com
 */
public interface ISocketAcceptor {
	/**
	 * 
	 * @param address
	 * @throws IOException
	 */
	public void bindSocket(SocketAddress address) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	public List<BindableSocket> getBoundSockets();
	
	/**
	 * 
	 */
	public void handleAcceptableConnections();
	
	/**
	 * 
	 * @return
	 */
	public IConnectionFilter getConnectionFilter();
	
	/**
	 * 
	 * @param filter
	 */
	public void setConnectionFilter(IConnectionFilter filter);
}
