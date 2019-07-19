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

import com.taurus.core.util.FixedIndexThreadPool;
import com.taurus.core.util.FixedIndexThreadPool.Work;
import com.taurus.core.util.Logger;
import com.taurus.core.util.Utils;
import com.taurus.permanent.core.BaseCoreService;
import com.taurus.permanent.core.BitSwarmEngine;
import com.taurus.permanent.core.DefaultConstants;
import com.taurus.permanent.core.SessionManager;
import com.taurus.permanent.data.Session;
import com.taurus.permanent.io.IOHandler;

/**
 * SocketReader
 * 
 * @author daixiwei daixiwei15@126.com
 */
public class SocketReader extends BaseCoreService implements Runnable {
	private final BitSwarmEngine	engine;
	private final Logger			logger;
	private int						threadPoolSize	= 1;
	private ExecutorService			threadPool;
	private FixedIndexThreadPool	packetReaderPool;
	private SessionManager			sessionManager;
	private SocketAcceptor			socketAcceptor;
	private SocketWriter			socketWriter;
	private Selector				readSelector;
	private IOHandler				ioHandler;
	private volatile boolean		isActive		= false;
	private volatile long			readBytes		= 0L;

	public SocketReader(int nThreads) {
		threadPoolSize = nThreads;

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
		int pr_count = packetReaderPool.shutdown();
		try {
			Thread.sleep(500L);

			readSelector.close();
		} catch (Exception e) {
			logger.warn("Error when shutting down TCP Selector: " + e.getMessage());
			logger.error(e);
		}

		logger.info("SocketReader stopped. Unprocessed tasks: " + leftOvers.size());
		logger.info("PacketReader stopped. Unprocessed tasks: " + pr_count);
	}

	public void initThreadPool() {
		threadPool = Executors.newSingleThreadExecutor();
		threadPool.execute(this);
		packetReaderPool = new FixedIndexThreadPool(threadPoolSize, "PacketReader", PacketReaderWork.class);
	}

	public void run() {
		// ByteBuffer readBuffer = Utils.allocateBuffer(engine.getConfig().maxReadBufferSize, engine.getConfig().readBufferType);
		Thread.currentThread().setName("SocketReader");

		while (isActive) {
			try {
				socketAcceptor.handleAcceptableConnections();
				readIncomingSocketData();

				Thread.sleep(5L);
			} catch (Throwable t) {
				logger.warn("Problems in SocketReader main loop: " + t + ", Thread: " + Thread.currentThread());
				logger.error(t);
			}
		}

		logger.info("SocketReader threadpool shutting down.");
	}

	private void readIncomingSocketData() {
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
					Session session = sessionManager.getSessionByConnection(channel);
					packetReaderPool.execute(session.getId(), session);
				}
			}

		} catch (ClosedSelectorException e) {
			logger.debug("Selector is closed!");
		} catch (CancelledKeyException localCancelledKeyException) {
		} catch (IOException ioe) {
			logger.warn("I/O reading/selection error: " + ioe);
			logger.error(ioe);
		} catch (Exception err) {
			logger.warn("Generic reading/selection error: " + err);
			logger.error(err);
		}
	}

	private void readTcpData(Session session,SocketChannel channel, ByteBuffer readBuffer) throws IOException {
		SelectionKey key = (SelectionKey) session.getSystemProperty(DefaultConstants.SESSION_SELECTION_KEY);
		if (!key.isValid()) {
			return;
		}
		if (key.isWritable()) {
			key.interestOps(SelectionKey.OP_READ);
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

	public static final class PacketReaderWork extends Work {
		private ByteBuffer		readBuffer;
		private BitSwarmEngine	engine;
		private SocketReader	reader;

		public PacketReaderWork() {
			engine = BitSwarmEngine.getInstance();
			reader = (SocketReader) engine.getSocketReader();
			readBuffer = Utils.allocateBuffer(engine.getConfig().maxReadBufferSize, engine.getConfig().readBufferType);
		}

		@Override
		protected void handlerTask(Object task) throws Exception {
			Session session = (Session) task;
			SocketChannel channel = (SocketChannel) session.getConnection().getChannel();
			readBuffer.clear();
			try {
				reader.readTcpData(session,channel, readBuffer);
			} catch (IOException e) {
				reader.closeConnection(channel);
			}
		}
	}
}
