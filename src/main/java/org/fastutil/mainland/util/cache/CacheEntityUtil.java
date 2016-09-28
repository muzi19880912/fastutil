package org.fastutil.mainland.util.cache;

import com.efun.mainland.util.redis.Redis;
import com.efun.mainland.util.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;

import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class CacheEntityUtil {
	private final static Logger logger = LoggerFactory.getLogger(CacheEntityUtil.class);
	private final static Timer timer = new Timer("delay-clear-delete-cache-timer");
	private final static long time = 10 * 60 * 1000;
	private final static ConcurrentHashMap<String, CacheEntity> map = new ConcurrentHashMap<String, CacheEntity>();

	static {
		try {
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					try {
						long startTime = System.currentTimeMillis();
						Set<Entry<String, CacheEntity>> set = map.entrySet();
						int before = map.size();
						for (Entry<String, CacheEntity> keyValue : set) {
							CacheEntity temp = keyValue.getValue();
							if (temp == null || temp.ttl() <= 0L) {
								map.remove(keyValue.getKey());
								logger.debug("timeout:key={}", keyValue.getKey());
							}
						}
						int after = map.size();
						logger.debug("cache size:before={},after={},time={}milliseconds", before, after,
								(System.currentTimeMillis() - startTime));
					} catch (Throwable e) {
						logger.error("Throwable:" + e.getMessage(), e);
					}

					final int pageSize = 5000;

					try {
						Set<String> tempSet = null;
						do {
							tempSet = RedisUtil.zrangeByScore(Redis.getCacheQueueKeyString(), 0,
									System.currentTimeMillis() - time, 0, pageSize);
							if (tempSet != null && tempSet.size() > 0) {
								String[] temps = new String[tempSet.size()];
								int i = 0;
								if (Redis.isCluster()) {
									for (String temp : tempSet) {
										temps[i] = temp;
										i++;
										Redis.loadCluster().del(temp);
										logger.debug("delete redis string key:" + temp);
									}
								} else {
									ShardedJedis redis = RedisUtil.loadRedis();
									try {
										for (String temp : tempSet) {
											temps[i] = temp;
											i++;
											redis.del(temp);
											logger.debug("delete redis string key:" + temp);
										}
									} finally {
										RedisUtil.returnRedis(redis);
									}
								}

								logger.info("delete redis string key size:" + i);
								RedisUtil.zrem(Redis.getCacheQueueKeyString(), temps);
							}
						} while (tempSet != null && tempSet.size() == pageSize);

						Set<byte[]> tempSet1 = null;
						do {
							tempSet1 = RedisUtil.zrangeByScore(Redis.getCacheQueueKeyByte(), 0,
									System.currentTimeMillis() - time, 0, pageSize);
							if (tempSet1 != null && tempSet1.size() > 0) {
								byte[][] temps = new byte[tempSet1.size()][];
								int i = 0;
								if (Redis.isCluster()) {
									for (byte[] temp : tempSet1) {
										temps[i] = temp;
										i++;
										Redis.loadCluster().del(temp);
									}
								} else {
									ShardedJedis redis = RedisUtil.loadRedis();
									try {
										for (byte[] temp : tempSet1) {
											temps[i] = temp;
											i++;
											redis.del(temp);
										}
									} finally {
										RedisUtil.returnRedis(redis);
									}
								}
								logger.info("delete redis byte key size:" + i);
								RedisUtil.zrem(Redis.getCacheQueueKeyByte(), temps);
							}
						} while (tempSet1 != null && tempSet1.size() == pageSize);

					} catch (Throwable e) {
						logger.error("Throwable:" + e.getMessage(), e);
					}
				}
			}, 1000 * 60, time);
		} catch (Throwable e) {
		}
	}

	public static final boolean containsKey(String key) {
		if (key != null) {
			return map.containsKey(key);
		} else {
			return false;
		}
	}

	public static final <T> void setCache(String key, CacheEntity<T> cacheEntity) {
		if (cacheEntity != null && key != null) {
			map.put(key, cacheEntity);
			logger.debug("set cache:key={},ttl={}milliseconds", key, cacheEntity.ttl());
		}
	}

	public static final void deleteCache(String key) {
		if (key != null) {
			map.remove(key);
			logger.debug("delete cache:key={}", key);
		}
	}

	@SuppressWarnings("unchecked")
	public static final <T> CacheEntity<T> getCache(String key) {
		CacheEntity cacheEntity = null;
		if (key != null) {
			cacheEntity = map.get(key);
			if (cacheEntity != null) {
				long ttl = cacheEntity.ttl();
				if (cacheEntity.ttl() <= 0L) {
					map.remove(key);
					cacheEntity = null;
					logger.debug("timeout:key={}", key);
				} else {
					logger.debug("get cache success:key={},ttl={}milliseconds", key, ttl);
				}
			}
		}
		return ((CacheEntity<T>) cacheEntity);
	}
}
