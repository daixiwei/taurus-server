package com.taurus.permanent.data;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.taurus.permanent.core.SessionManager;

/**
 * 核心用户session对象
 * @author daixiwei daixiwei15@126.com
 */
public final class Session {
	public static final String		DATA_BUFFER			= "session_data_buffer";
	public static final String		PACKET_READ_STATE	= "read_state";
	private static final String		NO_IP				= "NO_IP";


	private static AtomicInteger	idCounter			= new AtomicInteger(0);

	private volatile long			readBytes			= 0L;
	private volatile long			writtenBytes		= 0L;
	private volatile int			droppedMessages		= 0;
	private ISocketChannel			connection;
	private volatile long			creationTime;
	private volatile long			lastReadTime;
	private volatile long			lastWriteTime;
	private volatile long			lastActivityTime;
	private int						id;
	private volatile String			hashId;
	private SessionType				type;
	private volatile String			clientIpAddress 	= NO_IP;
	private volatile int			clientPort;
	private int						serverPort;
	private String					serverAddress;
	private int						timeout;
	private volatile boolean		frozen				= false;
	private boolean					markedForEviction	= false;
	private volatile boolean		connected			= false;
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
	public ISocketChannel getConnection() {
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
	 * 获取超时时间
	 * @return
	 */
	public int getTimeout() {
		return this.timeout;
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
	 * session 是否空闲，做超时踢出处理
	 * @return
	 */
	public boolean isIdle() {
		return isSocketIdle();
	}

	private boolean isSocketIdle() {
		boolean isIdle = false;

		if (this.timeout > 0) {
			long elapsedSinceLastActivity = System.currentTimeMillis() - this.lastActivityTime;
			isIdle = elapsedSinceLastActivity / 1000L > this.timeout;
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
	public void setConnection(ISocketChannel connection) {
		if (connection == null) {
			return;
		}

		if (this.connection != null) {
			throw new IllegalArgumentException("You cannot overwrite the connection linked to a Session!");
		}
		if(connection.checkConnection()) {
			setSocketConnection(connection);
			this.connected = true;
		}
	}

	private void setSocketConnection(ISocketChannel connection) {
		this.connection = connection;
		String svr_host = connection.getLocalAddress().toString().substring(1);
		String[] svr_adr =svr_host.split("\\:");
		this.serverAddress = svr_adr[0];
		try {
			this.serverPort = Integer.parseInt(svr_adr[1]);
		} catch (NumberFormatException localNumberFormatException) {
		}


		String client_host = connection.getRemoteAddress().toString().substring(1);
		String[] client_adr = client_host.split("\\:");
		this.clientIpAddress = client_adr[0];
		try {
			this.clientPort = Integer.parseInt(client_adr[1]);
		} catch (NumberFormatException localNumberFormatException) {
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
		this.lastReadTime = timestamp;
		if(this.hashId!=null) {
			this.lastActivityTime = timestamp;
		}
	}
	
	/**
	 * 设置最后写网络字节时间
	 * @param timestamp
	 */
	public void setLastWriteTime(long timestamp) {
		this.lastWriteTime = timestamp;
		if(this.hashId!=null) {
			this.lastActivityTime = timestamp;
		}
	}
	
	public void updateLastActivityTime() {
		this.lastActivityTime = System.currentTimeMillis();
	}

	/**
	 * session 标记为驱逐
	 */
	public void setMarkedForEviction() {
		this.markedForEviction = true;
		this.frozen = true;
	}

	/**
	 * 设置超时时间
	 * @param idleTime
	 */
	public void setTimeout(int idleTime) {
		this.timeout = idleTime;
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
			if(connection!=null) {
				connection.close();
			}
		} finally {
			connected = false;
			SessionManager.getInstance().removeSession(this);
		}
	}

	public String toString() {
		return String.format("{ Id: %s, Type: %s, IP: %s }", id + (hashId!=null ? ("[" + this.hashId + "]") : ""), type, getFullIpAddress());
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
