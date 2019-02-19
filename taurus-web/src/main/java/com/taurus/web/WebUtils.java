package com.taurus.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.core.util.Utils;

/**
 * WebUtils
 * @author daixiwei
 *
 */
public class WebUtils{
	private static final String								_POST		= "POST";
	static final String _Result = "$r";
	static final String _Param = "$p";
	
	
	static final ITObject httpRequest(HttpServletRequest request) throws IOException {
		String method = request.getMethod();
		if (!method.equals(_POST)) {
			return null;
		}
		InputStream in = request.getInputStream();
		byte[] bytes = Utils.uncompress(in);
		ITObject obj = TObject.newFromBinaryData(bytes);
		return obj;
	}
	
	final static void httpResponse(HttpServletResponse response, int result, ITObject param) throws IOException {
		ITObject tem = TObject.newInstance();
		tem.putInt(_Result, result);
		param = param == null ? TObject.newInstance() : param;
		tem.putTObject(_Param, param);
		byte[] bytes = tem.toBinary();
		OutputStream out = response.getOutputStream();
		Utils.compress(bytes, out);
		out.close();
	}

}
