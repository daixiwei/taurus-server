package com.taurus.permanent.normal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.taurus.core.util.Logger;
import com.taurus.core.util.Utils;
import com.taurus.permanent.core.BaseCoreService;
import com.taurus.permanent.core.BitSwarmEngine;
import com.taurus.permanent.core.SessionManager;
import com.taurus.permanent.data.Session;
import com.taurus.permanent.io.IOHandler;

/**
 * SocketReader
 * @author daixiwei daixiwei15@126.com
 */
public class SocketReader extends BaseCoreService implements Runnable {
	private final BitSwarmEngine	engine;
	private final Logger			logger;
	private int						threadPoolSize	= 1;
	private final ExecutorService	threadPool;
	private SessionManager			sessionManager;
	private SocketAcceptor			socketAcceptor;
	private SocketWriter			socketWriter;
	private Selector				readSelector;
	private IOHandler				ioHandler;
	private volatile boolean		isActive		= false;
	private volatile long			readBytes		= 0L;
	
	public SocketReader() {
		this(1);
	}
	
	public SocketReader(int nThreads) {
		threadPoolSize = nThreads;
		
		threadPool = Executors.newSingleThreadExecutor();
		
		engine = BitSwarmEngine.getInstance();
		logger = Logger.getLogger(getClass());
		try {
			readSelector = Selector.open();
			logger.info("TCP Selector opened");
		} catch (IOException e) {
			logger.error("Failed opening UDP Selector: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void init(Object o) {
		super.init(o);
		
		if (isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		
		sessionManager = engine.getSessionManager();
		socketAcceptor = engine.getSocketAcceptor();
		socketWriter = engine.getSocketWriter();
		
		isActive = true;
		initThreadPool();
		
		logger.info("IOHandler: " + ioHandler);
		logger.info("SocketReader started");
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		
		isActive = false;
		List<Runnable> leftOvers = threadPool.shutdownNow();
		try {
			Thread.sleep(500L);
			
			readSelector.close();
		} catch (Exception e) {
			logger.warn("Error when shutting down TCP Selector: " + e.getMessage());
			logger.error(e);
		}
		
		logger.info("SocketReader stopped. Unprocessed tasks: " + leftOvers.size());
	}
	
	public void initThreadPool() {
		for (int j = 0; j < threadPoolSize; j++) {
			threadPool.execute(this);
		}
	}
	
	public void run() {
		ByteBuffer readBuffer = Utils.allocateBuffer(engine.getConfig().maxReadBufferSize, engine.getConfig().readBufferType);
		Thread.currentThread().setName("SocketReader");
		
		while (isActive) {
			try {
				socketAcceptor.handleAcceptableConnections();
				readIncomingSocketData(readBuffer);
				
				Thread.sleep(5L);
			} catch (Throwable t) {
				logger.warn("Problems in SocketReader main loop: " + t + ", Thread: " + Thread.currentThread());
				logger.error( t);
			}
		}
		
		logger.info("SocketReader threadpool shutting down.");
	}
	
	private void readIncomingSocketData(ByteBuffer readBuffer) {
		SocketChannel channel = null;
		SelectionKey key = null;
		try {
			int readyKeyCount = readSelector.selectNow();
			
			if (readyKeyCount > 0) {
				Set<SelectionKey> readyKeys = readSelector.selectedKeys();
				
				for (Iterator<SelectionKey> it = readyKeys.iterator(); it.hasNext();) {
					key = (SelectionKey) it.next();
					it.remove();
					
					if (!key.isValid()) {
						continue;
					}
					channel = (SocketChannel) key.channel();
					
					readBuffer.clear();
					try {
						readTcpData(channel, key, readBuffer);
					} catch (IOException e) {
						closeConnection(channel);
					}
				}
			}
			
		} catch (ClosedSelectorException e) {
			logger.debug("Selector is closed!");
		} catch (CancelledKeyException localCancelledKeyException) {} catch (IOException ioe) {
			logger.warn("I/O reading/selection error: " + ioe);
			logger.error(ioe);
		} catch (Exception err) {
			logger.warn("Generic reading/selection error: " + err);
			logger.error( err);
		}
	}
	
	private void readTcpData(SocketChannel channel, SelectionKey key, ByteBuffer readBuffer) throws IOException {
		Session session = sessionManager.getSessionByConnection(channel);
		
		if (key.isWritable()) {
			key.interestOps(1);
			socketWriter.continueWriteOp(session);
		}
		if (!key.isReadable()) {
			return;
		}
		readBuffer.clear();
		long byteCount = channel.read(readBuffer);
		
		if (byteCount == -1L) {
			closeConnection(channel);
		} else if (byteCount > 0L) {
			session.setLastReadTime(System.currentTimeMillis());
			readBytes += byteCount;
			session.addReadBytes(byteCount);
			readBuffer.flip();
			byte[] binaryData = new byte[readBuffer.limit()];
			readBuffer.get(binaryData);
			ioHandler.onDataRead(session, binaryData);
		}
	}
	
	private void closeConnection(SelectableChannel channel) throws IOException {
		channel.close();
		if (channel instanceof SocketChannel)
			sessionManager.onSocketDisconnected(channel);
	}
	
	public IOHandler getIOHandler() {
		return ioHandler;
	}
	
	public Selector getSelector() {
		return readSelector;
	}
	
	public void setIoHandler(IOHandler handler) {
		if (handler == null) {
			throw new IllegalStateException("IOHandler si already set!");
		}
		ioHandler = handler;
	}
	
	public long getReadBytes() {
		return readBytes;
	}
	
	public long getReadPackets() {
		return ioHandler.getReadPackets();
	}
}
