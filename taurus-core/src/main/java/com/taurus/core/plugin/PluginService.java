package com.taurus.core.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;

public class PluginService {
	private static final String CONFIG_PATH = "config/taurus-core.xml";
	private final Logger logger;

	/**
	 * 插件列表
	 */
	final ConcurrentMap<String,IPlugin> pluginMap;
	
	private static PluginService _instance;
	
	/**
	 * get main instance
	 */
	public static PluginService me() {
		if (_instance == null) {
			_instance = new PluginService();
		}
		return _instance;
	}
	
	private PluginService() {
		logger = Logger.getLogger(PluginService.class);
		pluginMap = new ConcurrentHashMap<String,IPlugin>();
	}
	
	/**
	 * 加载配置
	 * @throws Exception
	 */
	public void loadConfig() throws Exception{
		loadConfig(System.getProperty("user.dir"));
	}
	
	private IPlugin createPlugin(String pclass) throws RuntimeException {
		if (StringUtil.isEmpty(pclass)) {
			logger.info("Plugin class not find!");
			return null;
		}
		IPlugin plugin = null;
		try {
			Class<?> extensionClass = Class.forName(pclass);
			if (!IPlugin.class.isAssignableFrom(extensionClass)) {
				throw new RuntimeException("Controller does not implement IPlugin! "+pclass);
			}
			plugin = (IPlugin) extensionClass.newInstance();
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Illegal access while instantiating class: " + pclass);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot instantiate class: " + pclass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found: " + pclass);
		}
		return plugin;
	}
	
	/**
	 * 加载配置
	 * @param path
	 * @throws Exception
	 */
	public void loadConfig(String path) throws Exception{
		File file = new File(path+"/"+CONFIG_PATH);
		if(!file.exists())return;
		InputStream is = new FileInputStream(file);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(is);
		Element root = document.getRootElement(); 
		
		String log4jPath = root.getChildTextTrim("log4jPath");
		if(StringUtil.isNotEmpty(log4jPath)) {
			try {
				Class.forName("org.apache.log4j.PropertyConfigurator");
				log4jPath = path+"/config/" + log4jPath;
				org.apache.log4j.PropertyConfigurator.configure(log4jPath);
			}catch (ClassNotFoundException e) {
			}
		}
		
		Iterator<?> itr = (root.getChildren("plugin")).iterator();
	    while(itr.hasNext()) {
	        Element pluginEm = (Element)itr.next();
			String pid = pluginEm.getChildTextTrim("id");
			if(StringUtil.isEmpty(pid)) {
				throw new RuntimeException("Plugin id is null!");
			}
			String pclass = pluginEm.getChildTextTrim("class");
			if(StringUtil.isEmpty(pclass)) {
				throw new RuntimeException("Plugin class is null!");
			}
			IPlugin plugin = createPlugin(pclass);
			plugin.setId(pid);
			plugin.loadConfig(pluginEm);
			putPlugin(plugin);
			logger.info("plugin["+plugin.getId()+"] load success!");
		}
	}
	
	/**
	 * put plugin instance
	 * @param plugin
	 */
	public final void putPlugin(IPlugin plugin) {
		if(pluginMap.containsKey(plugin.getId()))return;
		plugin.start();
		pluginMap.put(plugin.getId(),plugin);
	}
	
	/**
	 * remove plugin
	 * @param pluginId plugin key id
	 */
	public final void removePlugin(String pluginId) {
		IPlugin plugin =pluginMap.remove(pluginId);
		if(plugin!=null) {
			plugin.stop();
		}
	}

	/**
	 *  stop all plugin
	 */
	public final void stop() {
		Collection<IPlugin> list = pluginMap.values();
		for(IPlugin plugin : list) {
			plugin.stop();
		}
		pluginMap.clear();
	}
	
	
}
