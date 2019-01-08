package com.taurus.permanent.bitswarm.core;

import java.nio.channels.Selector;

import com.taurus.permanent.bitswarm.io.IOHandler;

/**
 * ISocketReader
 * @author daixiwei daixiwei15@126.com
 */
public interface ISocketReader {
	
	/**
	 * 
	 * @return
	 */
	public Selector getSelector();
	
	/**
	 * 
	 * @return
	 */
	public IOHandler getIOHandler();
	
	/**
	 * 
	 * @param iohandler
	 */
	public void setIoHandler(IOHandler iohandler);
	
	/**
	 * 
	 * @return
	 */
	public long getReadBytes();
	
	/**
	 * 
	 * @return
	 */
	public long getReadPackets();
}
