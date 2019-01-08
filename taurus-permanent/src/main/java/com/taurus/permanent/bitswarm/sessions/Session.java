package com.taurus.permanent.bitswarm.sessions;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 核心用户session对象
 * @author daixiwei daixiwei15@126.com
 */
public final class Session {
	public static final String		DATA_BUFFER			= "session_data_buffer";
	public static final String		PACKET_READ_STATE	= "read_state";
	private static final String		NO_IP				= "NO_IP";
	private static final String		SESSION_HASH		= "not";

	private static AtomicInteger	idCounter			= new AtomicInteger(0);

	private volatile long			readBytes			= 0L;
	private volatile long			writtenBytes		= 0L;
	private volatile int			droppedMessages		= 0;
	private SocketChannel			connection;
	private volatile long			creationTime;
	private volatile long			lastReadTime;
	private volatile long			lastWriteTime;
	private volatile long			lastActivityTime;
	private volatile long			lastLoggedInActivityTime;
	private int						id;
	private String					hashId				= SESSION_HASH;
	private SessionType				type;
	private volatile String			clientIpAddress;
	private volatile int			clientPort;
	private int						serverPort;
	private String					serverAddress;
	private int						maxIdleTime;
	private int						maxLoggedInIdleTime;
	private volatile boolean		frozen				= false;
	private boolean					markedForEviction	= false;
	private volatile boolean		connected			= false;
	private volatile boolean		loggedIn			= false;
	private IPacketQueue			packetQueue;
	private Map<String, Object>		systemProperties;

	public Session() {
		creationTime = lastReadTime = lastWriteTime = lastActivityTime = System.currentTimeMillis();

		setId(getUniqueId());

		systemProperties = new ConcurrentHashMap<String, Object>();
	}

	private static int getUniqueId() {
		return idCounter.incrementAndGet();
	}

	/**
	 * 统计当前session接收字节流大小
	 * @param amount
	 */
	public void addReadBytes(long amount) {
		this.readBytes += amount;
	}

	/**
	 * 统计当前session发送字节流大小
	 * @param amount
	 */
	public void addWrittenBytes(long amount) {
		this.writtenBytes += amount;
	}

	/**
	 * 获取channe连接对象
	 * @return
	 */
	public SocketChannel getConnection() {
		return this.connection;
	}

	/**
	 * 获取session创建时间
	 * @return
	 */
	public long getCreationTime() {
		return this.creationTime;
	}

	/**
	 * 获取hashid
	 * @return
	 */
	public String getHashId() {
		return this.hashId;
	}

	/**
	 * 获取session id
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * 获取客户端详细地址
	 * @return
	 */
	public String getFullIpAddress() {
		return clientPort > 0 ? getAddress() + ":" + clientPort : getAddress();
	}

	/**
	 * 获取客户端地址
	 * @return
	 */
	public String getAddress() {
		return this.clientIpAddress;
	}

	/**
	 * 获取客户端端口
	 * @return
	 */
	public int getClientPort() {
		return this.clientPort;
	}

	/**
	 * 获取服务器监听端口
	 * @return
	 */
	public int getServerPort() {
		return this.serverPort;
	}

	/**
	 * 获取服务器详细地址
	 * @return
	 */
	public String getFullServerIpAddress() {
		return this.serverAddress + ":" + this.serverPort;
	}

	/**
	 * 获取服务器地址
	 * @return
	 */
	public String getServerAddress() {
		return this.serverAddress;
	}

	/**
	 * 获取最后激活时间
	 * @return
	 */
	public long getLastActivityTime() {
		return this.lastActivityTime;
	}

	/**
	 * 获取最后读时间
	 * @return
	 */
	public long getLastReadTime() {
		return this.lastReadTime;
	}

	/**
	 * 获取最后写时间
	 * @return
	 */
	public long getLastWriteTime() {
		return this.lastWriteTime;
	}

	/**
	 * 获取最大空闲时间
	 * @return
	 */
	public int getMaxIdleTime() {
		return this.maxIdleTime;
	}

	/**
	 * 获取网络包队列
	 * @return
	 */
	public IPacketQueue getPacketQueue() {
		return this.packetQueue;
	}

	/**
	 * 获取服务器内部属性
	 * @param key
	 * @return
	 */
	public Object getSystemProperty(String key) {
		return this.systemProperties.get(key);
	}

	/**
	 * 获取属性
	 * @param key
	 */
	public void removeSystemProperty(String key) {
		this.systemProperties.remove(key);
	}

	/**
	 * 获取session类型
	 * @return
	 */
	public SessionType getType() {
		return this.type;
	}

	/**
	 * 获取当前session接收字节流大小
	 * @return
	 */
	public long getReadBytes() {
		return this.readBytes;
	}

	/**
	 * 获取当前session发送字节流大小
	 * @return
	 */
	public long getWrittenBytes() {
		return this.writtenBytes;
	}

	/**
	 * 当前连接状态
	 * @return
	 */
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * 设置连接状态
	 * @param value
	 */
	public void setConnected(boolean value) {
		this.connected = value;
	}

	/**
	 * session 是否被验证
	 * @return
	 */
	public boolean isLoggedIn() {
		return this.loggedIn;
	}

	/**
	 * 设置验证对象
	 * @param value
	 */
	public void setLoggedIn(boolean value) {
		this.loggedIn = value;
	}

	/**
	 * 获取验证以后session空闲时间
	 * @return
	 */
	public int getMaxLoggedInIdleTime() {
		return this.maxLoggedInIdleTime;
	}

	/**
	 * 设置验证以后session空闲时间
	 * @param idleTime
	 */
	public void setMaxLoggedInIdleTime(int idleTime) {
		if (idleTime < this.maxIdleTime) {
			idleTime = this.maxIdleTime + 60;
		}

		this.maxLoggedInIdleTime = idleTime;
	}

	/**
	 * 获取验证以后session激活时间
	 * @return
	 */
	public long getLastLoggedInActivityTime() {
		return this.lastLoggedInActivityTime;
	}

	/**
	 * 设置获取验证以后session激活时间
	 * @param timestamp
	 */
	public void setLastLoggedInActivityTime(long timestamp) {
		this.lastLoggedInActivityTime = timestamp;
	}

	/**
	 * session 是否空闲，做超时踢出处理
	 * @return
	 */
	public boolean isIdle() {
		if (this.loggedIn) {
			return isLoggedInIdle();
		}
		return isSocketIdle();
	}

	private boolean isSocketIdle() {
		boolean isIdle = false;

		if (this.maxIdleTime > 0) {
			long elapsedSinceLastActivity = System.currentTimeMillis() - this.lastActivityTime;
			isIdle = elapsedSinceLastActivity / 1000L > this.maxIdleTime;
		}

		return isIdle;
	}

	private boolean isLoggedInIdle() {
		boolean isIdle = false;

		if (this.maxLoggedInIdleTime > 0) {
			long elapsedSinceLastActivity = System.currentTimeMillis() - this.lastLoggedInActivityTime;
			isIdle = elapsedSinceLastActivity / 1000L > this.maxLoggedInIdleTime;
		}

		return isIdle;
	}

	public boolean isMarkedForEviction() {
		return this.markedForEviction;
	}

	/**
	 * 设置channel连接对象
	 * @param connection
	 */
	public void setConnection(SocketChannel connection) {
		if (connection == null) {
			return;
		}

		if (this.connection != null) {
			throw new IllegalArgumentException("You cannot overwrite the connection linked to a Session!");
		}
		setSocketConnection(connection);

	}

	private void setSocketConnection(SocketChannel connection) {
		this.connection = connection;
		this.serverPort = connection.socket().getLocalPort();
		this.serverAddress = connection.socket().getLocalAddress().toString().substring(1);

		if ((connection != null) && (connection.socket() != null) && (!connection.socket().isClosed())) {
			String hostAddr = connection.socket().getRemoteSocketAddress().toString().substring(1);
			String[] adr = hostAddr.split("\\:");
			this.clientIpAddress = adr[0];
			try {
				this.clientPort = Integer.parseInt(adr[1]);
			} catch (NumberFormatException localNumberFormatException) {
			}
			this.connected = true;
		} else {
			this.clientIpAddress = "[unknown]";
		}
	}

	/**
	 * 设置网络包队列
	 * @param queue
	 */
	public void setPacketQueue(IPacketQueue queue) {
		if (this.packetQueue != null) {
			throw new IllegalStateException("Cannot reassing the packet queue. Queue already exists!");
		}
		this.packetQueue = queue;
	}

	/**
	 * 设置创建时间
	 * @param timestamp
	 */
	public void setCreationTime(long timestamp) {
		this.creationTime = timestamp;
	}

	/**
	 * 设置hashID
	 * @param hash
	 */
	public void setHashId(String hash) {
		this.hashId = hash;
	}

	/**
	 * 设置session id
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 设置最后读取网络字节时间
	 * @param timestamp
	 */
	public void setLastReadTime(long timestamp) {
		this.lastReadTime = (this.lastActivityTime = timestamp);
	}

	/**
	 * 设置最后写网络字节时间
	 * @param timestamp
	 */
	public void setLastWriteTime(long timestamp) {
		this.lastWriteTime = (this.lastActivityTime = timestamp);
	}

	/**
	 * session 标记为驱逐
	 */
	public void setMarkedForEviction() {
		this.markedForEviction = true;
		this.frozen = true;
	}

	/**
	 * 设置最大空闲时间
	 * @param idleTime
	 */
	public void setMaxIdleTime(int idleTime) {
		this.maxIdleTime = idleTime;
	}

	/**
	 * 设置服务器内部属性
	 * @param key
	 * @param property
	 */
	public void setSystemProperty(String key, Object property) {
		this.systemProperties.put(key, property);
	}

	/**
	 * 设置session 类型
	 * @param type
	 */
	public void setType(SessionType type) {
		this.type = type;

		if (type == SessionType.VOID) {
			this.clientIpAddress = NO_IP;
			this.clientPort = 0;
		}
	}

	/**
	 * 获取丢包数量
	 * @return
	 */
	public int getDroppedMessages() {
		return this.droppedMessages;
	}

	/**
	 * 统计丢包数量 
	 */
	public void addDroppedMessages(int amount) {
		this.droppedMessages += amount;
	}

	/**
	 * session 是否被冻结
	 * @return
	 */
	public boolean isFrozen() {
		return this.frozen;
	}

	/**
	 * 关闭session
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.packetQueue = null;
		this.frozen = true;
		try {
			if ((type == SessionType.NORMAL) && (connection != null)) {
				Socket socket = connection.socket();

				if ((socket != null) && (!socket.isClosed())) {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
					connection.close();
				}

			} else if (type == SessionType.WEBSOCKET) {
			}

		} finally {
			connected = false;
			loggedIn = false;
			SessionManager.getInstance().removeSession(this);
		}
	}

	public String toString() {
		return String.format("{ Id: %s, Type: %s, IP: %s }", id + (loggedIn ? ("[" + this.hashId + "]") : ""), type, getFullIpAddress());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Session)) {
			return false;
		}
		boolean isEqual = false;
		Session session = (Session) obj;

		if (session.getId() == this.id) {
			isEqual = true;
		}
		return isEqual;
	}
}
