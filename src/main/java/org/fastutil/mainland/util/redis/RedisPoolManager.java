package org.fastutil.mainland.util.redis;


import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * 建议使用RedisAndSpringListener代替
 * @see RedisAndSpringListener
 */
@Deprecated
public class RedisPoolManager extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2073786379363537242L;
	private static final Logger log = Logger.getLogger(RedisPoolManager.class);

	public RedisPoolManager() {
		super();
	}

	public void destroy() {
		super.destroy();
		Redis.destroyPool();
		log.info("destroy redis pool");
	}

	public void init() throws ServletException {
		Redis.initPool();
		log.info("init redis pool");
	}

}
