package com.taurus.permanent.core;

/**   
 * TPEvents
* @author daixiwei daixiwei15@126.com
*/
public class TPEvents {
	/**
	 * session丢失或被清理时间
	 */
	public static final String	SESSION_LOST					= "sessionLost";
	/**
	 * session空闲事件
	 */
	public static final String	SESSION_IDLE					= "sessionIdle";
	/**
	 * 完成检测需要清理的session事件
	 */
	public static final String	SESSION_IDLE_CHECK_COMPLETE		= "sessionIdleCheckComplete";
	/**
	 * session断开连接事件
	 */
	public static final String EVENT_SESSION_DISCONNECT = "session_disconnect";
	
	
	public static final String PARAM_SESSION = "session";
	
}
