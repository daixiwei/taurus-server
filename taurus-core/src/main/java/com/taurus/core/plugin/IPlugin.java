
package com.taurus.core.plugin;

import org.jdom.Element;

/**
 * IPlugin
 */
public interface IPlugin {
	String getId();
	
	void setId(String id);
	
	/**
	 * 加载配置
	 * @param element
	 * @return
	 */
	boolean loadConfig(Element element);
	
	boolean start();
	boolean stop();
}
