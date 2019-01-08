package com.taurus.core.util.task;

/**
 * 任务处理器通用接口
 * @author daixiwei daixiwei15@126.com
 *
 */
public interface ITaskHandler {
	
	/**
	 * 
	 * @param task
	 * @throws Exception
	 */
	public void doTask(Task ask) throws Exception;
}
