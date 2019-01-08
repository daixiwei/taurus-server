package com.taurus.permanent.bitswarm.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.taurus.core.util.Logger;
import com.taurus.core.util.Utils;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.bitswarm.data.Packet;
import com.taurus.permanent.bitswarm.io.IOHandler;
import com.taurus.permanent.bitswarm.sessions.IPacketQueue;
import com.taurus.permanent.bitswarm.sessions.Session;
import com.taurus.permanent.bitswarm.sessions.SessionType;
import com.taurus.permanent.core.DefaultConstants;
import com.taurus.permanent.core.ServerConfig;

/**
 * SocketWriter
 * @author daixiwei
 *
 */
public final class SocketWriter extends BaseCoreService implements ISocketWriter, Runnable {
	private BitSwarmEngine					engine;
	private IOHandler						ioHandler;
	private final Logger					logger;
	private final ExecutorService			threadPool;
	private final BlockingQueue<Session>	sessionTicketsQueue;
	private volatile int					threadId				= 1;
	private volatile boolean				isActive				= false;
	private volatile long					droppedPacketsCount		= 0L;
	private volatile long					writtenBytes			= 0L;
	private volatile long					writtenPackets			= 0L;
	private int								threadPoolSize;
	
	public SocketWriter(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
		logger = Logger.getLogger(SocketWriter.class);
		
		sessionTicketsQueue = new LinkedBlockingQueue<Session>();
	}
	
	public void init(Object o) {
		super.init(o);
		
		if (isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		if (threadPoolSize < 1) {
			throw new IllegalArgumentException("Illegal value for a thread pool size: " + threadPoolSize);
		}
		engine = BitSwarmEngine.getInstance();
		isActive = true;
		
		initThreadPool();
		
		logger.info("Socket Writer started (pool size:" + threadPoolSize + ")");
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		
		isActive = false;
		List<Runnable> leftOvers = threadPool.shutdownNow();
		logger.info("SocketWriter stopped. Unprocessed tasks: " + leftOvers.size());
	}
	
	public int getQueueSize() {
		return sessionTicketsQueue.size();
	}
	
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
	
	public IOHandler getIOHandler() {
		return ioHandler;
	}
	
	public void setIOHandler(IOHandler ioHandler) {
		if (this.ioHandler != null) {
			throw new IllegalStateException("You cannot reassign the IOHandler class!");
		}
		this.ioHandler = ioHandler;
	}
	
	public void continueWriteOp(Session session) {
		if (session != null)
			sessionTicketsQueue.add(session);
	}
	
	private void initThreadPool() {
		for (int j = 0; j < threadPoolSize; j++)
			threadPool.execute(this);
	}
	
	public void run() {
		Thread.currentThread().setName("SocketWriter-" + threadId++);
		
		ServerConfig setting = TaurusPermanent.getInstance().getConfig();
		ByteBuffer writeBuffer = Utils.allocateBuffer(setting.maxWriteBufferSize, setting.writeBufferType);
		
		while (isActive) {
			try {
				Session session = sessionTicketsQueue.take();
				processSessionQueue(writeBuffer, session);
			} catch (InterruptedException e) {
				logger.warn("SocketWriter thread interrupted: " + Thread.currentThread());
				isActive = false;
			} catch (Throwable t) {
				logger.warn("Problems in SocketWriter main loop, Thread: " + Thread.currentThread());
				logger.error(t);
			}
		}
		
		logger.info("SocketWriter threadpool shutting down.");
	}
	
	private void processSessionQueue(ByteBuffer writeBuffer, Session session) {
		if (session != null) {
			SessionType type = session.getType();
			
			if (type == SessionType.NORMAL) {
				processRegularSession(writeBuffer, session);
			}else if (type == SessionType.VOID)
				return;
		}
	}
	
	private void processRegularSession(ByteBuffer writeBuffer, Session session) {
		if (session.isFrozen()) {
			return;
		}
		Packet packet = null;
		try {
			IPacketQueue sessionQ = session.getPacketQueue();
			
			synchronized (sessionQ) {
				if (!sessionQ.isEmpty()) {
					packet = sessionQ.peek();
					
					if (packet == null) {
						return;
					}
					tcpSend(writeBuffer, sessionQ, session, packet);
				}
			}
			
		} catch (ClosedChannelException cce) {
			logger.debug("Socket closed during write operation for session: " + session);
		} catch (IOException localIOException) {} catch (Exception e) {
			logger.warn("Error during write. Session: " + session);
			logger.error(e);
		}
	}
	
	private void tcpSend(ByteBuffer writeBuffer, IPacketQueue sessionQ, Session session, Packet packet) throws Exception {
		SocketChannel channel = session.getConnection();
		if (channel == null) {
			logger.debug("Skipping packet, found null socket for Session: " + session);
			return;
		}
		writeBuffer.clear();
		
		byte[] buffer = packet.isFragmented() ? packet.getFragmentBuffer() : (byte[]) packet.getData();
		if (writeBuffer.capacity() < buffer.length) {
			writeBuffer = Utils.allocateBuffer(buffer.length, engine.getConfig().writeBufferType);
		}
		writeBuffer.put(buffer);
		writeBuffer.flip();
		
		long toWrite = writeBuffer.remaining();
		
		long bytesWritten = channel.write(writeBuffer);
		
		writtenBytes += bytesWritten;
		session.addWrittenBytes(bytesWritten);
		if (bytesWritten < toWrite) {
			byte[] bb = new byte[writeBuffer.remaining()];
			writeBuffer.get(bb);
			packet.setFragmentBuffer(bb);
			
			SelectionKey sk = (SelectionKey) session.getSystemProperty(DefaultConstants.SESSION_SELECTION_KEY);
			if ((sk != null) && (sk.isValid())) {
				sk.interestOps(5);
			} else {
				logger.warn("Could not OP_WRITE for Session: " + session + ", written bytes: " + bytesWritten);
			}
		} else {
			writtenPackets += 1L;
			
			sessionQ.take();
			if (!sessionQ.isEmpty()) {
				sessionTicketsQueue.add(session);
			}
		}
	}
	
	public void enqueuePacket(Packet packet) {
		enqueueLocal(packet);
	}
	
	private void enqueueLocal(Packet packet) {
		Collection<Session> recipients = packet.getRecipients();
		int size = recipients.size();
		
		if ((recipients != null) && (size > 0)) {
			if (packet.getSender() != null) {
				packet.getSender().setLastWriteTime(System.currentTimeMillis());
			}
			if (size == 1) {
				enqueueLocalPacket((Session) packet.getRecipients().iterator().next(), packet);
			} else
				for (Session session : recipients) {
					enqueueLocalPacket(session, packet.clone());
				}
		}
	}
	
	private void enqueueLocalPacket(Session session, Packet packet) {
		IPacketQueue sessionQ = session.getPacketQueue();
		
		if (sessionQ != null) {
			synchronized (sessionQ) {
				try {
					boolean wasEmpty = sessionQ.isEmpty();
					
					sessionQ.put(packet);
					
					if ((wasEmpty)) {
						sessionTicketsQueue.add(session);
					}
					
					packet.setRecipients(null);
				} catch (Exception error) {
					dropOneMessage(session);
				}
			}
		}
	}
	
	/**
	 * 丢包处理
	 * @param session
	 */
	private void dropOneMessage(Session session) {
		session.addDroppedMessages(1);
		droppedPacketsCount += 1L;
	}
	
	public long getDroppedPacketsCount() {
		return droppedPacketsCount;
	}
	
	
	public long getWrittenBytes() {
		return writtenBytes;
	}
	
	public long getWrittenPackets() {
		return writtenPackets;
	}
}
