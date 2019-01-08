package com.taurus.core.util;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Logger日志类
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class Logger {
	private static final String	NULL		= "";
	private static final String ROOT_LOGGER_NAME = "ROOT";
	
	private static Class<?> _customClass;
	private String _name=ROOT_LOGGER_NAME;
	
	/**
	 * 设置自定义Class
	 * @param className
	 */
	public static final void setCustomClass(Class<?> customClass) {
		_customClass = customClass;
	}
	
	public static final Logger getLogger(Class<?> log_class) {
		Logger logger = null;
		if(_customClass!=null) {
			if(Logger.class.isAssignableFrom(_customClass)) {
				try {
					Constructor<?> c1=_customClass.getDeclaredConstructor(new Class[]{Class.class});
					c1.setAccessible(true);
					logger = (Logger) c1.newInstance(log_class);
					return logger;
				}  catch (Exception e) {
					e.printStackTrace();
				}  
			}
		}
		try {
			Class.forName("org.apache.log4j.Logger");
			logger = new Log4j(log_class);
			return logger;
		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
		}
		logger = new Logger(log_class);
		return logger;
	}
	
	protected Logger(Class<?> log_class) {
		_name = log_class==null?ROOT_LOGGER_NAME : log_class.getSimpleName();
	}
	
	public String getName() {
		return _name;
	}
	
	private void printThrowable(PrintStream print,Throwable throwable) {
		print.println(throwable);
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace)
        	print.println("\tat " + traceElement);
	}
	
	protected void print(LoggerLevel level,String str,Throwable throwable){
        Calendar calendar = Calendar.getInstance();  
		calendar.setTime(new Date());  
	    String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime()); 
	    str = StringUtil.isEmpty(str)?NULL:str;
        str = String.format("%s  %-5s  [%s] %s    -%s", date, level.name(),Thread.currentThread().getName(), _name, str);
        System.out.println(str);
        if(throwable!=null) {
        	printThrowable(System.out,throwable);
        }
    }
	
	public void trace(String msg) {
		print(LoggerLevel.TRACE,msg,null);
	}

	public void trace(String msg, Object... params) {
		String str = String.format(msg, params);
		trace(str);
	}

	public void debug(String msg) {
		print(LoggerLevel.DEBUG,msg,null);
	}

	public void debug(String msg, Object... params) {
		String str = String.format(msg, params);
		debug(str);
	}

	public void info(String msg) {
		print(LoggerLevel.INFO,msg,null);
	}

	public void info(String msg, Object... params) {
		String str = String.format(msg, params);
		info(str);
	}


	public void warn(String msg) {
		print(LoggerLevel.WARN, msg,null);
	}

	public void warn(String msg, Object... params) {
		String str = String.format(msg, params);
		warn(str);
	}


	public void error(String msg) {
		print(LoggerLevel.ERROR,msg,null);
	}
	
	public void error(String msg, Object... params) {
		String str = String.format(msg, params);
		error(str);
	}
	
	public void error(Throwable throwable) {
		error(NULL,throwable);
	}
	
	public void error(String msg, Throwable throwable) {
		print(LoggerLevel.ERROR,msg,throwable);
	}
	

	/**
	 * 日志等级
	 * @author daixiwei	daixiwei15@126.com
	 *
	 */
	public static enum LoggerLevel{
		DEBUG,
		INFO,
		WARN,
		ERROR,
		TRACE
	}
	
	/**
	 * log4j 扩展
	 * @author daixiwei	daixiwei15@126.com
	 */
	public static final class Log4j extends Logger{
		private org.apache.log4j.Logger logger;
		
		protected Log4j(Class<?> log_class) {
			super(log_class);
			logger = org.apache.log4j.Logger.getLogger(log_class);
		}
		
		@Override
		protected void print(LoggerLevel level,String str,Throwable throwable){
			switch (level) {
			case DEBUG:
				logger.debug(str);
				break;
			case ERROR:
				logger.error(str,throwable);
				break;
			case WARN:
				logger.warn(str);
				break;
			case TRACE:
				logger.trace(str);
				break;
			case INFO:
				logger.info(str);
				break;
			}
		}
	}
}
