package com.taurus.core.util.executor;


/**
 * Taurus Thread pool config
 * @author daixiwei
 *
 */
public class ExecutorConfig {
	/**
	 * 线程池名称
	 */
	public String				name;
	/**
	 * 核心线程数
	 */
	public int					coreThreads;
	/**
	 * 备用线程数
	 */
	public int					backupThreads;
	/**
	 * 最大备用线程数
	 */
	public int					maxBackups;
	/**
	 * 大于队列大小触发备用线程
	 */
	public int					queueSizeTriggeringBackup;
	/**
	 * 触发备用线程时间(秒)
	 */
	public int					secondsTriggeringBackup;
	/**
	 * 备份线程过期时间 (秒)
	 */
	public int					backupThreadsExpiry;
	/**
	 * 满足队列大小备用线程过期
	 */
	public int					queueSizeTriggeringBackupExpiry;

	public ExecutorConfig() {
		this.name = null;
		this.coreThreads = 16;
		this.backupThreads = 8;
		this.maxBackups = 2;
		this.queueSizeTriggeringBackup = 500;
		this.secondsTriggeringBackup = 60;
		this.backupThreadsExpiry = 3600;
		this.queueSizeTriggeringBackupExpiry = 300;
	}
}