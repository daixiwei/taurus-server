package com.taurus.permanent.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 服务器配置信息
 * @author daixiwei daixiwei15@126.com
 */
public class ServerConfig {
	public volatile List<SocketAddress>	socketAddresses					= new ArrayList<SocketAddress>();
	public volatile IpFilterConfig		ipFilter						= new IpFilterConfig();
	public volatile int					timerThreadPoolSize				= 1;
	public volatile int					protocolCompression				= 512;

	public String						readBufferType					= "HEAP";
	public String						writeBufferType					= "HEAP";
	public int							maxPacketSize					= 4096;
	public int							maxReadBufferSize				= 1024;
	public int							maxWriteBufferSize				= 32768;
	public int							socketAcceptorThreadPoolSize	= 1;
	public int							socketReaderThreadPoolSize		= 1;
	public int							socketWriterThreadPoolSize		= 1;
	public int							sessionPacketQueueSize			= 120;
	public int							sessionTimeout					= 15;
	public boolean						tcpNoDelay						= false;

	public ExecutorConfig				systemThreadPoolConfig			= new ExecutorConfig();
	public ExecutorConfig				extensionThreadPoolConfig		= new ExecutorConfig();
	public ExtensionConfig				extensionConfig					= new ExtensionConfig();
	public WebSocketConfig				webSocketConfig					= new WebSocketConfig();
	
	/**
	 * ip过滤设置
	 * @author daixiwei daixiwei15@126.com
	 */
	public static final class IpFilterConfig {
		public List<String>	addressBlackList			= new ArrayList<String>();
		public List<String>	addressWhiteList			= new ArrayList<String>();
		public volatile int	maxConnectionsPerAddress	= 99999;
	}

	/**
	 * server ip_port绑定
	 * @author daixiwei daixiwei15@126.com
	 */
	public static final class SocketAddress {
		public static final String	TYPE_UDP	= "UDP";
		public static final String	TYPE_TCP	= "TCP";
		public String				address		= "127.0.0.1";
		public int					port		= 9339;
		public String				type		= TYPE_TCP;

		public String toString() {
			return String.format("[%s]%s:%d", type, address, port);
		}
	}
	
	/**
	 * Taurus Thread pool config
	 * @author daixiwei
	 *
	 */
	public static final class ExecutorConfig {
		/**
		 * 线程池名称
		 */
		public String				name;
		/**
		 * 核心线程大小
		 */
		public int					corePoolSize=4;
		/**
		 * 最大线程大小
		 */
		public int					maxPoolSize=16;
		/**
		 * 线程最大空闲时间(毫秒)
		 */
		public int					keepAliveTime =60000;
		/**
		 * 最大队列大小
		 */
		public int					maxQueueSize=20000;
	}
	
	/**
	 * 自定义启动控制设置
	 * @author daixiwei daixiwei15@126.com
	 *
	 */
	public static final class ExtensionConfig {
		public String	name			= "";
		public String	className		= "";
	}
	
	/**
	 * web socket
	 * @author daixiwei daixiwei15@126.com
	 *
	 */
	public static final class WebSocketConfig {
		public boolean isActive = true;
	}

	private static final void loadThreadPoolConfig(Element em,ExecutorConfig config) {
		config.name = em.getChildTextTrim("name");
		config.corePoolSize = Integer.parseInt(em.getChildTextTrim("corePoolSize"));
		config.maxPoolSize = Integer.parseInt(em.getChildTextTrim("maxPoolSize"));
		config.keepAliveTime = Integer.parseInt(em.getChildTextTrim("keepAliveTime"));
		config.maxQueueSize = Integer.parseInt(em.getChildTextTrim("maxQueueSize"));
	}
	
	public final void load(InputStream is) throws Exception{
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(is);
		Element root = document.getRootElement(); 
		
		this.timerThreadPoolSize = Integer.parseInt(root.getChildTextTrim("timerThreadPoolSize"));
		this.protocolCompression = Integer.parseInt(root.getChildTextTrim("protocolCompression"));
		this.readBufferType = root.getChildTextTrim("readBufferType");
		this.writeBufferType = root.getChildTextTrim("readBufferType");
		this.maxPacketSize = Integer.parseInt(root.getChildTextTrim("maxPacketSize"));
		this.maxReadBufferSize = Integer.parseInt(root.getChildTextTrim("maxReadBufferSize"));
		this.maxWriteBufferSize = Integer.parseInt(root.getChildTextTrim("maxWriteBufferSize"));
		this.socketAcceptorThreadPoolSize = Integer.parseInt(root.getChildTextTrim("socketAcceptorThreadPoolSize"));
		this.socketReaderThreadPoolSize = Integer.parseInt(root.getChildTextTrim("socketWriterThreadPoolSize"));
		this.socketWriterThreadPoolSize = Integer.parseInt(root.getChildTextTrim("socketWriterThreadPoolSize"));
		this.maxPacketSize = Integer.parseInt(root.getChildTextTrim("maxPacketSize"));
		this.sessionPacketQueueSize = Integer.parseInt(root.getChildTextTrim("sessionPacketQueueSize"));
		this.sessionTimeout = Integer.parseInt(root.getChildTextTrim("sessionTimeout"));
		this.tcpNoDelay = Boolean.parseBoolean(root.getChildTextTrim("tcpNoDelay"));
		
		Element addressesEm = root.getChild("socketAddresses");
		Iterator<?> itr = (addressesEm.getChildren("socket")).iterator();
	    while(itr.hasNext()) {
	        Element socketEm = (Element)itr.next();
	        SocketAddress sa = new SocketAddress();
	        sa.address =  socketEm.getAttributeValue("address", "0.0.0.0");
	        sa.port = Integer.parseInt(socketEm.getAttributeValue("port", "9339"));
	        sa.type = socketEm.getAttributeValue("type", SocketAddress.TYPE_TCP);
	        socketAddresses.add(sa);
	    }
	    
	    
	    Element ipFilterEm = root.getChild("ipFilter");
	    Element addressBlackListEm = ipFilterEm.getChild("addressBlackList");
	    itr = (addressBlackListEm.getChildren("string")).iterator();
	    while(itr.hasNext()) {
	    	Element socketEm = (Element)itr.next();
	    	ipFilter.addressBlackList.add(socketEm.getTextTrim());
	    }
	    Element addressWhiteListEm = ipFilterEm.getChild("addressWhiteList");
	    itr = (addressWhiteListEm.getChildren("string")).iterator();
	    while(itr.hasNext()) {
	    	Element socketEm = (Element)itr.next();
	    	ipFilter.addressWhiteList.add(socketEm.getTextTrim());
	    }
	    ipFilter.maxConnectionsPerAddress = Integer.parseInt(ipFilterEm.getChildTextTrim("maxConnectionsPerAddress"));
	    
	    Element extensionConfigEm = root.getChild("extensionConfig");
	    extensionConfig.className = extensionConfigEm.getChildTextTrim("className");
	    extensionConfig.name = extensionConfigEm.getChildTextTrim("name");
	    
	    
	    Element webSocketEm = root.getChild("webSocket");
	    webSocketConfig.isActive = Boolean.parseBoolean(webSocketEm.getChildTextTrim("isActive"));
	    
	    loadThreadPoolConfig(root.getChild("systemThreadPoolConfig"),systemThreadPoolConfig);
	    
	    loadThreadPoolConfig(root.getChild("extensionThreadPoolConfig"),extensionThreadPoolConfig);
	}
}
