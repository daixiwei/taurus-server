package com.taurus.permanent.bitswarm.core;

import com.taurus.permanent.bitswarm.data.Packet;
import com.taurus.permanent.bitswarm.io.IOHandler;
import com.taurus.permanent.bitswarm.sessions.Session;

/**
 * ISocketWriter
 * @author daixiwei daixiwei15@126.com
 */
public interface ISocketWriter {
	public IOHandler getIOHandler();
	
	public void setIOHandler(IOHandler handler);
	
	public void continueWriteOp(Session session);
	
	public void enqueuePacket(Packet packet);
	
	public long getDroppedPacketsCount();
	
	public long getWrittenBytes();
	
	public long getWrittenPackets();
	
	public int getQueueSize();
	
	public int getThreadPoolSize();
}
