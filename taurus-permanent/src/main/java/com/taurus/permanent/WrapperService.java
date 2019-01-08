package com.taurus.permanent;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;
import org.tanukisoftware.wrapper.event.WrapperControlEvent;
import org.tanukisoftware.wrapper.event.WrapperEvent;
import org.tanukisoftware.wrapper.event.WrapperEventListener;

/**
 * 使用wrapper启用系统服务
 * <p>
 * <a href="https://wrapper.tanukisoftware.com">warpper官网</a>
 * 
 * @author daixiwei daixiwei15@126.com
 */
public class WrapperService implements WrapperListener, WrapperEventListener {
	private long m_eventMask = 0xffffffffffffffffL;

	WrapperService() {
		updateEventListener();
	}

	protected void updateEventListener() {
		WrapperManager.removeWrapperEventListener(this);
		WrapperManager.addWrapperEventListener(this, m_eventMask);
	}

	/*---------------------------------------------------------------
	 * WrapperListener Methods
	 *-------------------------------------------------------------*/
	public Integer start(String[] args) {
		System.out.println("Taurus-permanent Service: start()");

		TaurusPermanent taurus = TaurusPermanent.getInstance();
		taurus.start();

		return null;
	}

	public int stop(int exitCode) {
		System.out.println("Taurus-permanent Service: stop(" + new Integer(exitCode));
		TaurusPermanent.getInstance().shutdown();
		System.exit(0);
		return exitCode;
	}
	
	@Override
	public void controlEvent(int event) {
		System.out.println(String.format("Taurus-permanent Service: controlEvent(%s)", event));

		if (event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT) {
			if (WrapperManager.isLaunchedAsService() || WrapperManager.isIgnoreUserLogoffs()) {
				System.out.println("Taurus-permanent Service:   Ignoring logoff event");
				// Ignore
			} else {
				WrapperManager.stop(0);
			}
		} else if (event == WrapperManager.WRAPPER_CTRL_C_EVENT) {
			// WrapperManager.stop(0);
		} else {
			WrapperManager.stop(0);
		}
	}
	
	/*---------------------------------------------------------------
	* WrapperEventListener Methods
	*-------------------------------------------------------------*/
	/**
	 * Called whenever a WrapperEvent is fired. The exact set of events that a listener will receive will depend on the mask supplied when WrapperManager.addWrapperEventListener
	 * was called to register the listener.
	 *
	 * Listener implementations should never assume that they will only receive events of a particular type. To assure that events added to future versions of the Wrapper do not
	 * cause problems with user code, events should always be tested with "if ( event instanceof {EventClass} )" before casting it to a specific event type.
	 *
	 * @param event WrapperEvent which was fired.
	 */
	public void fired(WrapperEvent event) {
		System.out.println("Received event:" + event);
		if (event instanceof WrapperControlEvent) {
			System.out.println("Consume and ignore.");
			((WrapperControlEvent) event).consume();
		}
	}

	
}
