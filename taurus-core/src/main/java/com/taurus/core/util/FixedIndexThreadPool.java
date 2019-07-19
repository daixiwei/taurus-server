package com.taurus.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * fixed index thread pool class.
 * 
 * @author daixiwei daixiwei15@126.com
 *
 */
public class FixedIndexThreadPool {
	private int					poolSize;
	private String				poolName;
	private volatile boolean	_run;
	private List<Work>			works;

	public FixedIndexThreadPool(int poolSize, String poolName, Class<? extends Work> workClass) {
		this.poolSize = poolSize;
		this.poolName = poolName;
		_run = true;
		works = new ArrayList<FixedIndexThreadPool.Work>();
		try {
			for (int i = 0; i < poolSize; ++i) {
				Work work = workClass.newInstance();
				work.init(this, i);
				works.add(work);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * execute task
	 * 
	 * @param index
	 * @param task
	 */
	public void execute(int index, Object task) {
		Work work = works.get(index % poolSize);
		work.addTask(task);
	}

	/**
	 * shut down all work
	 * 
	 * @return
	 */
	public synchronized int shutdown() {
		int count = 0;
		this._run = false;
		for (Work work : works) {
			count += work.stop();
		}
		return count;
	}

	public boolean isRun() {
		return _run;
	}

	/**
	 * get thread pool name
	 * 
	 * @return
	 */
	public String getPoolName() {
		return poolName;
	}

	/**
	 * Thread work class
	 */
	public static abstract class Work implements Runnable {
		protected FixedIndexThreadPool	pool;
		protected Thread				thread;
		protected BlockingQueue<Object>	taskQueue;
		protected Logger				log;

		public void init(FixedIndexThreadPool pool, int id) {
			this.pool = pool;
			this.thread = new Thread(this, pool.poolName + "-" + id);
			this.thread.start();
			taskQueue = new LinkedBlockingQueue<Object>();
			log = Logger.getLogger(this.getClass());
		}

		/**
		 * 添加任务
		 * @param task
		 */
		public void addTask(Object task) {
			if (task != null) {
				taskQueue.add(task);
			}
		}

		/**
		 * stop work
		 * @return
		 */
		public synchronized int stop() {
			if (thread != null) {
				thread.interrupt();
				thread = null;
			}
			return taskQueue.size();
		}

		/**
		 * 处理任务
		 * @param task
		 * @throws Exception
		 */
		protected abstract void handlerTask(Object task) throws Exception;

		@Override
		public void run() {
			while (pool.isRun()) {
				try {
					Object task = taskQueue.take();
					if (task != null) {
						handlerTask(task);
					}
				} catch (InterruptedException e) {
					log.error(e);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}
}
