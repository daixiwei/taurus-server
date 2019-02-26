
package com.taurus.web;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import com.taurus.core.util.StringUtil;



/**
 * JettyServer is used to config and start jetty web server.
 * Jetty version 8.1.8
 */
class JettyServer{
	private static final int DEFAULT_PORT = 80;
	private static final String DEFAULT_WEBAPPDIR = "webapp";
	
	private String webAppDir;
	private int port;
	private String context;
	private boolean running = false;
	private Server server;
	private WebAppContext webApp;

	JettyServer(){
		this(DEFAULT_WEBAPPDIR,DEFAULT_PORT,"/");
	}
	
	JettyServer(String webAppDir, int port, String context) {
		if (webAppDir == null) {
			throw new IllegalStateException("Invalid webAppDir of web server: " + webAppDir);
		}
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid port of web server: " + port);
		}
		if (StringUtil.isEmpty(context)) {
			throw new IllegalStateException("Invalid context of web server: " + context);
		}
		
		this.webAppDir = webAppDir;
		this.port = port;
		this.context = context;
	}
	
	public void start() {
		if (!running) {
			try {
				running = true;
				doStart();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public void stop() {
		if (running) {
			try {server.stop();} catch (Exception e) {e.printStackTrace();}
			running = false;
		}
	}
	
	private void doStart() {
		if (!available(port)) {
			throw new IllegalStateException("port: " + port + " already in use!");
		}
		
		System.out.println("Starting mpnet web server ");
		server = new Server(port);
		webApp = new WebAppContext();
		webApp.setThrowUnavailableOnStartupException(true);
		webApp.setContextPath(context);
		webApp.setResourceBase(webAppDir);
		webApp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		webApp.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		
		server.setHandler(webApp);
		changeClassLoader(webApp);
		
		
		try {
			System.out.println("Starting web server on port: " + port);
			server.start();
			System.out.println("Starting Complete.");
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return;
	}
	
	private void changeClassLoader(WebAppContext webApp) {
		try {
			WebAppClassLoader jfcl = new WebAppClassLoader(webApp);
			webApp.setClassLoader(jfcl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean available(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
		
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ds != null) {
				ds.close();
			}
			
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	
	
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			JettyServer server = new JettyServer();
			server.start();
			return;
		}
		
		if (args.length == 3) {
			String webAppDir = args[0];
			int port = Integer.parseInt(args[1]);
			String context = args[2];
			JettyServer server = new JettyServer(webAppDir, port, context);
			server.start();
			return ;
		}
	}
	
}