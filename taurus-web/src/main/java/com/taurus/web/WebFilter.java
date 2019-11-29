package com.taurus.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.util.StringUtil;

/**
 * 
 * main filter class
 *
 */
public class WebFilter implements Filter {
	private static final String	_UTF8			= "UTF-8";
	private static final String	_ALLOW_ORIGIN	= "Access-Control-Allow-Origin";
	private static final String	_ALLOW_ORIGIN_V	= "*";
	private int					contextPathLength;

	private TWebServer			tweb;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			String contextPath = filterConfig.getServletContext().getContextPath();
			contextPathLength = StringUtil.isNotEmpty(contextPath) ? contextPath.length() : 0;
			tweb = TWebServer.me();
			tweb.init(filterConfig);
		} catch (Exception e) {
			throw new ServletException(e);
		}
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
		tweb.handle(target, request, response, chain);
	}

	@Override
	public void destroy() {
		tweb.destroy();
	}

}
