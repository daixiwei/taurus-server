package com.taurus.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.entity.ITObject;
import com.taurus.core.plugin.PluginService;
import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;


/**
 * 
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class WebFilter implements Filter {
	private static final String	_UTF8	= "UTF-8";
	private static final String	_POST	= "POST";
	private static final String	_ALLOW_ORIGIN	= "Access-Control-Allow-Origin";
	private static final String	_ALLOW_ORIGIN_V	= "*";
	static final String _Session = "$s";
	static final String _Version = "$v";
	
	private int					contextPathLength;
	private Extension			extension;
	private ActionMapping		actionMapping;
	public 	static int			forceVer	= 1;
	private Logger log;
	

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			String path = filterConfig.getServletContext().getRealPath("/");
			System.setProperty("WORKDIR", path);
			PluginService.me().loadConfig(path);
			log = Logger.getLogger(WebFilter.class);
			String contextPath = filterConfig.getServletContext().getContextPath();
			contextPathLength = StringUtil.isNotEmpty(contextPath) ? contextPath.length() : 0;
			Routes routes = new Routes() {public void config() {}};
			actionMapping = new ActionMapping(routes);
			
			String extensionClass = filterConfig.getInitParameter("main");
			this.extension = instanceExtension(extensionClass);
			this.extension.configRoute(routes);
			actionMapping.buildActionMapping();
			this.extension.onStart();
		} catch (Exception e) {
			throw new ServletException(e);
		}
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

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		String target = request.getRequestURI();
		if (StringUtil.isEmpty(target)) {
			return;
		}
		if (contextPathLength != 0) {
			target = target.substring(contextPathLength);
		}
		request.setCharacterEncoding(_UTF8);
		response.setCharacterEncoding(_UTF8);
		response.setHeader(_ALLOW_ORIGIN, _ALLOW_ORIGIN_V);
		Action action = actionMapping.getAction(target);

		if (action != null) {
			String method = request.getMethod();
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
			
			String session = null;
			if (obj.containsKey(_Session)) {
				session = obj.getString(_Session);
			}
			ITObject params = obj.getTObject(WebUtils._Param);
			Controller controller=null;
			try {
				controller = action.getControllerClass().newInstance();
				controller._init(request,response,action.getActionKey(), session, params);
		
				if(action.getInterceptor()!=null) {
					if(action.getInterceptor().intercept(action, controller)) {
						action.getMethod().invoke(controller);
					}
				}else {
					action.getMethod().invoke(controller);
				}
			}catch (InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				if (targetException instanceof WebException) {
					WebException we = (WebException)targetException;
					controller.sendResponse(we.getCode(), null);
				}else {
					controller.sendResponse(500, null);
					log.error(targetException);
				}
			}catch(Exception e) {
				if(controller!=null) {
					controller.sendResponse(500, null);
				}
				log.error(e);
			}
		}else {
			chain.doFilter(req, res);
		}
	}
	
	@Override
	public void destroy() {
		PluginService.me().stop();
//		main.destroy();
	}

}
