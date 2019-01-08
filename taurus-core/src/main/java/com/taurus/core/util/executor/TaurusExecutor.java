package com.taurus.core.util.executor;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.taurus.core.util.Logger;

/**
 * Taurus Thread pool executor
 * @author daixiwei
 *
 */
public class TaurusExecutor extends ThreadPoolExecutor {
	private final Logger			logger;
	private final ExecutorConfig	cfg;
	private final int				maxThreads;
	private final int				backupThreadsExpirySeconds;
	private volatile long			lastQueueCheckTime;
	private volatile long			lastBackupTime;
	private volatile boolean		threadShutDownNotified;
	private long					lastAlertTime;
	private static final long		ALERT_INTERVAL	= 30000L;

	public TaurusExecutor(final ExecutorConfig config) {
		super(config.coreThreads, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new TPThreadFactory(config.name));
		this.threadShutDownNotified = false;
		this.cfg = config;
		this.logger = Logger.getLogger(this.getClass());
		this.maxThreads = this.cfg.coreThreads + this.cfg.backupThreads * this.cfg.maxBackups;
		this.backupThreadsExpirySeconds = this.cfg.backupThreadsExpiry * 1000;
		this.lastQueueCheckTime = -1L;
	}

	@Override
	public void execute(final Runnable command) {
		if (this.getPoolSize() >= this.cfg.coreThreads) {
			final boolean needsBackup = this.checkQueueWarningLevel();
			if (needsBackup) {
				if (this.getPoolSize() >= this.maxThreads) {
					this.alertLowThreads();
				} else {
					this.setCorePoolSize(this.getPoolSize() + this.cfg.backupThreads);
					final long currentTimeMillis = System.currentTimeMillis();
					this.lastQueueCheckTime = currentTimeMillis;
					this.lastBackupTime = currentTimeMillis;
					this.threadShutDownNotified = false;
					this.logger.info(String.format("Added %s new threads, current size is: %s", this.cfg.backupThreads, this.getPoolSize()));
				}
			} else if (this.getPoolSize() > this.cfg.coreThreads) {
				final boolean isTimeToShutDownBackupThreads = System.currentTimeMillis() - this.lastBackupTime > this.backupThreadsExpirySeconds;
				final boolean isQueueSizeSmallEnough = this.getQueue().size() < this.cfg.queueSizeTriggeringBackupExpiry;
				if (isTimeToShutDownBackupThreads && isQueueSizeSmallEnough && !this.threadShutDownNotified) {
					this.setCorePoolSize(this.cfg.coreThreads);
					this.threadShutDownNotified = true;
					this.logger.info("Shutting down old backup threads");
				}
			}
		}
		super.execute(command);
	}

	private boolean checkQueueWarningLevel() {
		boolean needsBackup = false;
		final boolean queueIsBusy = this.getQueue().size() >= this.cfg.queueSizeTriggeringBackup;
		final long now = System.currentTimeMillis();
		if (this.lastQueueCheckTime < 0L) {
			this.lastQueueCheckTime = now;
		}
		if (queueIsBusy) {
			if (now - this.lastQueueCheckTime > this.cfg.secondsTriggeringBackup * 1000) {
				needsBackup = true;
			}
		} else {
			this.lastQueueCheckTime = now;
		}
		return needsBackup;
	}

	private void alertLowThreads() {
		final long now = System.currentTimeMillis();
		if (now > this.lastAlertTime + ALERT_INTERVAL) {
			this.logger.warn(String.format("%s :: Queue size is big: %s, but all backup thread are already active: %s", this.cfg.name, this.getQueue().size(), this.getPoolSize()));
			this.lastAlertTime = now;
		}
	}

	public int getCoreThreads() {
		return this.cfg.coreThreads;
	}

	public int getBackupThreads() {
		return this.cfg.backupThreads;
	}

	public int getMaxBackups() {
		return this.cfg.maxBackups;
	}

	public int getQueueSizeTriggeringBackup() {
		return this.cfg.queueSizeTriggeringBackup;
	}

	public int getSecondsTriggeringBackup() {
		return this.cfg.secondsTriggeringBackup;
	}

	public int getBackupThreadsExpiry() {
		return this.cfg.backupThreadsExpiry;
	}

	public int getQueueSizeTriggeringBackupExpiry() {
		return this.cfg.queueSizeTriggeringBackupExpiry;
	}

	private static final class TPThreadFactory implements ThreadFactory {
		private static final AtomicInteger	POOL_ID;
		private static final String			THREAD_BASE_NAME	= "Worker:%s:%s";
		private final AtomicInteger			threadId;
		private final String				poolName;

		static {
			POOL_ID = new AtomicInteger(0);
		}

		public TPThreadFactory(final String poolName) {
			this.threadId = new AtomicInteger(1);
			this.poolName = poolName;
			TPThreadFactory.POOL_ID.incrementAndGet();
		}

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(r,
					String.format(THREAD_BASE_NAME, (this.poolName != null) ? this.poolName : TPThreadFactory.POOL_ID.get(), this.threadId.getAndIncrement()));
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != 5) {
				t.setPriority(5);
			}
			return t;
		}
	}
}
