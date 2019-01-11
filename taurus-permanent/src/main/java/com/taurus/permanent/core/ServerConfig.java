package com.taurus.permanent.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.taurus.core.util.executor.ExecutorConfig;

/**
 * 服务器配置信息
 * @author daixiwei daixiwei15@126.com
 */
public class ServerConfig {
	public volatile List<SocketAddress>	socketAddresses					= new ArrayList<SocketAddress>();
	public volatile IpFilterConfig	ipFilter						= new IpFilterConfig();
	public volatile int					timerThreadPoolSize				= 1;
	public volatile int					protocolCompressionThreshold	= 300;

	public volatile boolean				useDebugMode					= false;
	public volatile boolean				useFriendlyExceptions			= true;

	public String						readBufferType					= "HEAP";
	public String						writeBufferType					= "HEAP";
	public int							maxIncomingRequestSize			= 4096;
	public int							maxReadBufferSize				= 1024;
	public int							maxWriteBufferSize				= 32768;
	public int							socketAcceptorThreadPoolSize	= 1;
	public int							socketReaderThreadPoolSize		= 1;
	public int							socketWriterThreadPoolSize		= 1;
	public int							sessionPacketQueueSize			= 120;
	public int							sessionMaxIdleTime;
	public int							userMaxIdleTime;
	public boolean						tcpNoDelay						= false;

	public volatile boolean				ghostHunterEnabled				= true;
	public ExecutorConfig				systemThreadPoolConfig			= new ExecutorConfig();
	public ExecutorConfig				extensionThreadPoolConfig			= new ExecutorConfig();
	public ControllerSettings			controllerSettings				= new ControllerSettings();
	public WebServerConfig				webServerConfig					= new WebServerConfig();
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
	 * 自定义启动控制设置
	 * @author daixiwei daixiwei15@126.com
	 *
	 */
	public static final class ControllerSettings {
		public String	name			= "";
		public String	className		= "";
	}
	
	/**
	 * web server
	 * @author daixiwei daixiwei15@126.com
	 *
	 */
	public static final class WebServerConfig {
		public boolean isActive;
	}

	private static final void loadThreadPoolConfig(Element em,ExecutorConfig config) {
		config.name = em.getChildTextTrim("name");
		config.coreThreads = Integer.parseInt(em.getChildTextTrim("coreThreads"));
		config.backupThreads = Integer.parseInt(em.getChildTextTrim("backupThreads"));
		config.maxBackups = Integer.parseInt(em.getChildTextTrim("maxBackups"));
		config.queueSizeTriggeringBackup = Integer.parseInt(em.getChildTextTrim("queueSizeTriggeringBackup"));
		config.secondsTriggeringBackup = Integer.parseInt(em.getChildTextTrim("secondsTriggeringBackup"));
		config.backupThreadsExpiry = Integer.parseInt(em.getChildTextTrim("backupThreadsExpiry"));
		config.queueSizeTriggeringBackupExpiry = Integer.parseInt(em.getChildTextTrim("queueSizeTriggeringBackupExpiry"));
	}
	
	public final void load(InputStream is) throws Exception{
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(is);
		Element root = document.getRootElement(); 
		
		this.timerThreadPoolSize = Integer.parseInt(root.getChildTextTrim("timerThreadPoolSize"));
		
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
//	    ipFilterEm.getChild("addressBlackList");
//	    itr = (addressesEm.getChildren("socket")).iterator();
//	    while(itr.hasNext()) {
//	    	
//	    }
	    ipFilter.maxConnectionsPerAddress = Integer.parseInt(ipFilterEm.getChildTextTrim("maxConnectionsPerAddress"));
	    
	    Element controllerSettingsEm = root.getChild("controllerSettings");
	    controllerSettings.className = controllerSettingsEm.getChildTextTrim("className");
	    controllerSettings.name = controllerSettingsEm.getChildTextTrim("name");
	    
	    
	    Element webServerEm = root.getChild("webServer");
	    
	    loadThreadPoolConfig(root.getChild("systemThreadPoolConfig"),systemThreadPoolConfig);
	    
	    loadThreadPoolConfig(root.getChild("extensionThreadPoolConfig"),extensionThreadPoolConfig);
	}
}
