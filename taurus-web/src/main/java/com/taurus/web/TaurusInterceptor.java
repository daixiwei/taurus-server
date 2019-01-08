package com.taurus.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.taurus.core.entity.*;
import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;
import com.taurus.core.util.Utils;

/**
 * TaurusInterceptor
 * @author daixiwei
 *
 */
public class TaurusInterceptor implements Interceptor{
	private static final String								_POST		= "POST";
	private static final String								_RESULT		= "result";
	private static final String								_DATA		= "data";
	private static final String								_SESSION	= "session_id";
	private static final String								_VERSION	= "ver";
	private static final Logger log = Logger.getLogger(TaurusInterceptor.class);
	
	@Override
	public void intercept(Invocation inv) {
		Controller ctr = inv.getController();
		if(!(ctr instanceof TaurusController)) {
			log.error("Controller is not extends TaurusController!");
			return;
		}
		TaurusController taurusCtr = (TaurusController)ctr;
		try {
			ITObject parms = httpRequest(taurusCtr.getRequest());
			if(parms==null)return;
		} catch (IOException e) {
			log.error("Read data execption!\n",e);
			return;
		}
		inv.invoke();
	}
	
	private static final ITObject httpRequest(HttpServletRequest request) throws IOException {
		String method = request.getMethod();
		if (!method.equals(_POST)) {
			return null;
		}
		InputStream in = request.getInputStream();
		byte[] bytes = Utils.uncompress(in);
		in.close();
		String json = new String(bytes, 0, bytes.length, StringUtil.UTF_8);

		if (StringUtil.isEmpty(json)) {
			return null;
		}
		ITObject obj = TObject.newFromJsonData(json);
		return obj;
	}
	
	final static void httpResponse(HttpServletResponse response, int result, ITObject data) throws IOException {
		ITObject tem = TObject.newInstance();
		tem.putInt(_RESULT, result);
		data = data == null ? TObject.newInstance() : data;
		tem.putTObject(_DATA, data);
		String json = tem.toJson();
		OutputStream out = response.getOutputStream();
		Utils.compress(StringUtil.getBytes(json), out);
		out.close();
	}

}
