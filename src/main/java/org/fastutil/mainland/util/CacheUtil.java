package org.fastutil.mainland.util;

import com.efun.mainland.util.cache.TimeUnitSeconds;
import com.efun.mainland.util.redis.Redis;
import com.efun.mainland.util.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 缓存操作工具类
 * 
 * @author Administrator
 *
 */
public class CacheUtil {
	private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

	private static final String cachePrefix = PropertiesCacheUtil.getValue("cachePrefix", Redis.REDIS_CONFIG_FILE);

	static {
		System.out.println("cachePrefix:" + cachePrefix);
		logger.info("cachePrefix:{}", cachePrefix);
	}

	/**
	 * 获取缓存前缀（各地区区域标识）
	 * 
	 * @return
	 */
	public final static String getCachePrefix() {
		return cachePrefix;
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 * @return
	 */
	public static final Object getCacheByKey(String key) {
		return getCacheByKey(key, null);
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 * @param classType
	 * @return
	 */
	public static final <T> T getCacheByKey(String key, Class<T> classType) {

		try {
			return CommonUtil.get(RedisUtil.get(loadKey(classType, key)), classType);
		} catch (Exception e) {
			logger.error(classType.getName() + " Exception:" + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 添加缓存
	 * 
	 * @see addCache(String key, T t, Class<T> classType, int seconds)
	 * @param key
	 * @param t
	 * @param classType
	 * @return
	 */
	@Deprecated
	public static final <T> boolean addCache(String key, T t, Class<T> classType) {
		return addCache(key, t, classType, TimeUnitSeconds.CACHE_HALF_YEAR);
	}

	/**
	 * 更新缓存
	 * 
	 * @see addCache(String key, T t, Class<T> classType, int seconds)
	 * @param key
	 * @param t
	 * @param classType
	 * @return
	 */
	@Deprecated
	public static final <T> boolean updateCache(String key, T t, Class<T> classType) {
		return addCache(key, t, classType);
	}

	/**
	 * 添加缓存
	 * 
	 * @see addCache(String key, T t, Class<T> classType, int seconds)
	 * @param key
	 * @param t
	 * @param classType
	 * @param effectiveDate
	 * @return
	 */
	@Deprecated
	public static final <T> boolean addCache(String key, T t, Class<T> classType, Date effectiveDate) {

		Date date = new Date();
		try {
			String result = RedisUtil.setex(loadKey(classType, key),
					(int) (effectiveDate.getTime() - date.getTime()) / 1000, CommonUtil.serialize(t));
			return "OK".equals(result);
		} catch (Exception e) {
			logger.error(classType.getName() + " Serializable no:" + e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 更新缓存<br/>
	 * 
	 * @see addCache(String key, T t, Class<T> classType, int seconds)
	 * 
	 * @param key
	 * @param t
	 * @param classType
	 * @param effectiveDate
	 * @return
	 */
	@Deprecated
	public static final <T> boolean updateCache(String key, T t, Class<T> classType, Date effectiveDate) {
		return addCache(key, t, classType, effectiveDate);
	}

	/**
	 * 添加缓存
	 * 
	 * @param key
	 * @param t
	 * @param classType
	 * @param seconds
	 * @return
	 */
	public static final <T> boolean addCache(String key, T t, Class<T> classType, int seconds) {
		try {
			String result = RedisUtil.setex(loadKey(classType, key), seconds, CommonUtil.serialize(t));
			return "OK".equals(result);
		} catch (Exception e) {
			logger.error(classType.getName() + " Serializable no:" + e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 更新缓存<br/>
	 * 
	 * @see addCache(String key, T t, Class<T> classType, int seconds)
	 * @param key
	 * @param t
	 * @param classType
	 * @param seconds
	 * @return
	 */
	public static final <T> boolean updateCache(String key, T t, Class<T> classType, int seconds) {
		return addCache(key, t, classType, seconds);
	}

	/**
	 * 删除缓存
	 * 
	 * @param key
	 * @param classType
	 * @return
	 */
	public static final <T> boolean deleteCache(String key, Class<T> classType) {

		try {
			return RedisUtil.del(loadKey(classType, key),true) > 0L;
		} catch (Exception e) {
			logger.error(classType.getName() + " :" + e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 删除缓存
	 * 
	 * @param key
	 * @return
	 */
	public static final boolean deleteCache(String key) {
		return deleteCache(key, null);
	}

	/**
	 * 加载key值
	 * 
	 * @param cls
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static final <T> byte[] loadKey(Class<T> cls, String key) throws Exception {
		return CommonUtil.stringToByte(loadStringKey(cls, key));
	}

	/**
	 * 加载key值
	 * 
	 * @param cls
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static final <T> String loadStringKey(Class<T> cls, String key) throws Exception {
		StringBuilder strBuilder = new StringBuilder(80);
		strBuilder.append(cachePrefix);
		if (cls != null) {
			strBuilder.append("-").append(cls.getName());
		}
		strBuilder.append("-").append(key);
		return strBuilder.toString();
	}

	/**
	 * key值
	 * 
	 * @param cls
	 * @param key
	 * @param other
	 * @return
	 */
	public static final <T> String loadObjectMessageKey(Class<T> cls, String key, String other) {
		StringBuilder strBuilder = new StringBuilder(80);
		strBuilder.append(cachePrefix);
		if (cls != null) {
			strBuilder.append("-").append(cls.getSimpleName());
		}
		strBuilder.append("-Msg-").append(key);
		if (other != null) {
			strBuilder.append("-").append(other);
		}
		return strBuilder.toString();
	}

}
