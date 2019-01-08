package com.taurus.core.util.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.taurus.core.service.AbstractService;
import com.taurus.core.util.Logger;

/**
 * 计时器/任务调度器
 * @author daixiwei daixiwei15@126.com
 *
 */
public class TaskScheduler extends AbstractService implements Runnable {
	private static AtomicInteger		schedulerId	= new AtomicInteger(0);
	
	private volatile int				threadId	= 1;
	private long						SLEEP_TIME	= 250L;
	private ExecutorService				taskExecutor;
	private LinkedList<ScheduledTask>	taskList;
	private LinkedList<ScheduledTask>	addList;
	private Logger						logger;
	private volatile boolean			running		= false;
	
	public TaskScheduler() {
		schedulerId.incrementAndGet();
		this.taskList = new LinkedList<ScheduledTask>();
		this.addList = new LinkedList<ScheduledTask>();
		this.logger = Logger.getLogger(TaskScheduler.class);
	}
	
	public void init(Object o) {
		super.init(o);
		startService();
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		stopService();
	}
	
	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("not supported in this class version");
	}
	
	public void startService() {
		running = true;
		taskExecutor = Executors.newSingleThreadExecutor();
		taskExecutor.execute(this);
	}
	
	public void stopService() {
		running = false;
		List<?> leftOvers = taskExecutor.shutdownNow();
		taskExecutor = null;
		logger.info("Scheduler stopped. Unprocessed tasks: " + leftOvers.size());
	}
	
	public void run() {
		Thread.currentThread().setName("Scheduler" + schedulerId.get() + "-thread-" + this.threadId++);
		logger.info("Scheduler started: " + name);
		
		while (running) {
			try {
				executeTasks();
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ie) {
				logger.warn("Scheduler: " + name + " interrupted.");
			} catch (Exception e) {
				logger.error( "Scheduler: " + name + " caught a generic exception: " + e, e);
			}
		}
	}
	
	public void addScheduledTask(Task task, int interval, boolean loop, ITaskHandler callback) {
		synchronized (addList) {
			addList.add(new ScheduledTask(task, interval, loop, callback));
		}
	}
	
	private void executeTasks() {
		long now = System.currentTimeMillis();
		
		if (taskList.size() > 0) {
			synchronized (taskList) {
				for (Iterator<ScheduledTask> it = taskList.iterator(); it.hasNext();) {
					ScheduledTask t = (ScheduledTask) it.next();
					
					if (!t.task.isActive()) {
						it.remove();
					} else {
						if (now < t.expiry) {
							continue;
						}
						try {
							t.callback.doTask(t.task);
						} catch (Exception e) {
							logger.error("Scheduler callback exception. Callback: " + t.callback + ", Exception: " + e, e);
						}
						
						if (t.loop) {
							t.expiry += t.interval * 1000;
						} else {
							it.remove();
						}
					}
				}
			}
			
		}
		
		if (addList.size() > 0) {
			synchronized (taskList) {
				taskList.addAll(addList);
				addList.clear();
			}
		}
	}
	
	private final class ScheduledTask {
		long			expiry;
		int				interval;
		boolean			loop;
		ITaskHandler	callback;
		Task			task;
		
		public ScheduledTask(Task t, int interval, boolean loop, ITaskHandler callback) {
			this.task = t;
			this.interval = interval;
			this.expiry = (System.currentTimeMillis() + interval * 1000);
			this.callback = callback;
			this.loop = loop;
		}
	}
}
