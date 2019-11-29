package com.taurus.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.entity.ITObject;
import com.taurus.core.plugin.PluginService;
import com.taurus.core.routes.Action;
import com.taurus.core.routes.ActionMapping;
import com.taurus.core.routes.Extension;
import com.taurus.core.routes.Routes;
import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;

/**
 * The web server main class.
 * 
 * @author daixiwei daixiwei15@126.com
 *
 */
public class TWebServer {
	private static final String			_POST		= "POST";
	private static final String			_Session	= "$s";
	private static final String			_Version	= "$v";
	/**
	 * The server class instance.
	 */
	private static TWebServer			_instance	= null;
	private Logger						log;
	private Extension					extension;
	private ActionMapping				actionMapping;
	private ScheduledThreadPoolExecutor	timeScheduler;
	private int							forceVer	= 1;
	private Routes						routes;
	private String						contextPath;
	private String						contextRealPath;
	/**
	 * 当前并发数
	 */
	private final List<SessionInfo>		concurrentList;
	
	/**
	 * get main instance
	 */
	public static TWebServer me() {
		if (_instance == null) {
			_instance = new TWebServer();
		}
		return _instance;
	}

	public TWebServer() {
		log = Logger.getLogger(WebFilter.class);
		this.routes =  new Routes(Routes.CONTROLLER_CLASS) {
			public void config() {
			}
		};
		concurrentList = new ArrayList<SessionInfo>();
		timeScheduler = new ScheduledThreadPoolExecutor(1);
		actionMapping = new ActionMapping(routes);
	}

	void init(FilterConfig filterConfig) throws Exception {
		String path = filterConfig.getServletContext().getRealPath("/");
		System.setProperty("WORKDIR", path);
		PluginService.me().loadConfig(path);
		this.contextRealPath = path;
		this.contextPath = filterConfig.getServletContext().getContextPath();
		String extensionClass = filterConfig.getInitParameter("main");
		this.extension = instanceExtension(extensionClass);
		this.extension.configRoute(routes);
		actionMapping.buildActionMapping();
		this.extension.onStart();
		timeScheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				forceVer = extension.readVersion();
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	private Extension instanceExtension(String extensionClass) {
		if (StringUtil.isEmpty(extensionClass)) {
			throw new RuntimeException("Extension class parameter is missing!");
		}
		Extension extension = null;
		try {
			Class<?> exclass = Class.forName(extensionClass);
			if (!Extension.class.isAssignableFrom(exclass)) {
				throw new RuntimeException("You extension does not implement Extension ");
			}
			extension = (Extension) exclass.newInstance();
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Illegal access while instantiating class: " + extensionClass);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot instantiate class: " + extensionClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found: " + extensionClass);
		}
		return extension;
	}

	void handle(String target,HttpServletRequest request,HttpServletResponse response,FilterChain chain)
			throws IOException, ServletException {
		String method = request.getMethod();
		SessionInfo info = new SessionInfo();
		info.target = target;
		info.method = method;
		synchronized (concurrentList) {
			concurrentList.add(info);
		}
		Action action =actionMapping.getAction(target);
		try {
			if (action != null) {
				if (!method.equals(_POST)) {
					return;
				}
	
				ITObject obj = WebUtils.httpRequest(request);
				if (obj == null) {
					throw new RuntimeException("data is null!");
				}
				if (!obj.containsKey(_Version)) {
					WebUtils.httpResponse(response, -1, null);
					return;
				}
				int client_ver = obj.getInt(_Version);
				if (client_ver < forceVer) {
					WebUtils.httpResponse(response, -1, null);
					return;
				}
				long startTime = System.currentTimeMillis();
				String session = null;
				if (obj.containsKey(_Session)) {
					session = obj.getString(_Session);
				}
				ITObject params = obj.getTObject(WebUtils._Param);
				Controller controller = null;
				try {
					controller = (Controller)action.getControllerClass().newInstance();
					controller._init(request, response, action.getActionKey(), session, params);
	
					if (action.getInterceptor() != null) {
						action.getInterceptor().intercept(action, controller);
					} 
					action.getMethod().invoke(controller);
				} catch (InvocationTargetException e) {
					Throwable targetException = e.getTargetException();
					if (targetException instanceof WebException) {
						WebException we = (WebException) targetException;
						controller.sendResponse(we.getCode(), null);
					} else {
						controller.sendResponse(500, null);
						log.error(targetException);
					}
				}catch (WebException e) {
					controller.sendResponse(e.getCode(), null);
				}catch (Exception e) {
					if (controller != null) {
						controller.sendResponse(500, null);
					}
					log.error(e);
				}
				long endTime = System.currentTimeMillis();
				log.info("action: "+action + "[" + session+"] time:"+(endTime - startTime)+"ms");
			} else {
				chain.doFilter(request, response);
			}
		}finally {
			synchronized (concurrentList) {
				concurrentList.remove(info);
			}
		}
	}
	void destroy() {
		if (timeScheduler != null) {
			timeScheduler.shutdownNow();
		}
		if (extension != null) {
			extension.onStop();
		}
		PluginService.me().stop();
		synchronized (concurrentList) {
			concurrentList.clear();
		}
	}

	public int getForceVer() {
		return forceVer;
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public String getContextRealPath() {
		return contextRealPath;
	}
	
	public List<SessionInfo> getConcurrentList(){
		return concurrentList;
	}
}
