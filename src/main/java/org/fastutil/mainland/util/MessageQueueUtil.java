package org.fastutil.mainland.util;

import com.efun.mainland.util.redis.RedisUtil;
import org.apache.log4j.Logger;


public class MessageQueueUtil {
	private static final int MAX_REPEAT_COUNT = 3;
	private static final Logger logger = Logger.getLogger(MessageQueueUtil.class);

	/**
	 * 数据入队（从队尾入队）
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static final Long rMessageQueueIn(String key, String... value) {
		Long result = 0L;
		int currentRepeatCount = 1;
		boolean writeLog = true;
		while (currentRepeatCount <= MAX_REPEAT_COUNT) {
			currentRepeatCount++;
			result = RedisUtil.rpush(key, value);
			if (result != null && result.longValue() > 0L) {
				writeLog = false;
				break;
			}
		}
		if (writeLog && value != null) {
			for (String str : value) {
				logger.info("into queue fail,key>>>>>" + key);
				logger.info("into queue fail,value>>>>>" + str);
			}
		}
		return result;
	}

	/**
	 * 数据出队（从队头出队）
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static final String lMessageQueueOut(String key) {
		return RedisUtil.lpop(key);
	}
	
}
