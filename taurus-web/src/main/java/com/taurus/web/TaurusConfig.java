package com.taurus.web;

import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Routes;
import com.jfinal.kit.PathKit;
import com.taurus.core.plugin.PluginService;

/**
 * TaurusConfig
 * @author daixiwei
 *
 */
public abstract class TaurusConfig extends JFinalConfig{
	
	public void configConstant(Constants me) {
		System.out.println(PathKit.getWebRootPath());
	}
	
	public void configRoute(Routes me) {
		TaurusRoutes trs = new TaurusRoutes();
		configTaurusRoute(trs);
		me.add(trs);
	}
	
	/**
	 * 配置 taurus-web 路由
	 * @param me
	 */
	protected abstract void configTaurusRoute(Routes me);
	
	@Override
	public void beforeJFinalStop() {
		PluginService.getInstance().stop();
	}
}
