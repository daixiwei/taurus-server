package com.taurus.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.plugin.redis.Cache;
import com.taurus.core.plugin.redis.Redis;
import com.taurus.core.util.StringUtil;

import redis.clients.jedis.JedisPool;

@WebServlet("/status")
public class StatusServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String str = "<html><body>";
		String tem = req.getParameter("info");
		boolean is_info = false;
		if(StringUtil.isNotEmpty(tem)) {
			is_info = Boolean.parseBoolean(tem);
		}
		str += "connect-num:"+WebFilter.concurrentList.size() + "<br>";
		if(is_info) {
			List<SessionInfo> list = null;
			synchronized (WebFilter.concurrentList) {
				list = new ArrayList<>(WebFilter.concurrentList);
			}
			int i= 0;
			for(SessionInfo info : list) {
				i++;
				str +=String.format("<br><b>[%s] action->%s  method->%s</b><br>",i, info.target,info.method);
			}
		}
		
		ConcurrentHashMap<String, Cache> map = Redis.getCacheMap();
		Set<Entry<String,Cache>> entrySet = map.entrySet();
		for(Entry<String,Cache> entry : entrySet) {
			JedisPool pool = entry.getValue().getJedisPool();
			str += String.format("<br><b>%s</b><br>",entry.getKey());
			str += "active-num:"+pool.getNumActive();
			str += "\tidle-num:"+pool.getNumIdle();
			str += "\twait-num:"+pool.getNumWaiters();
		}
		str+="</body></html>";
		
		resp.getWriter().write(str);
	}
}
