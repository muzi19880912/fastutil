package org.fastutil.mainland.util.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

/**
 * 集成redis和spring context启动和关闭时的资源管理
 *
 * @author Created by fastutil on 2016-08-19.
 * @see ContextLoaderListener
 *
 */
public class RedisAndSpringListener extends ContextLoaderListener {

    protected static final Logger logger= LoggerFactory.getLogger(RedisAndSpringListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        beforeInitialized();
        super.contextInitialized(event);
        afterInitialized();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        beforeDestroyed();
        super.contextDestroyed(event);
        afterDestroyed();
    }

    protected void redisInitialized(){
        Redis.initPool();
        System.out.println("inited redis pool");
        logger.info("inited redis pool");
    }
    protected void redisDestroyed(){
        Redis.destroyPool();
        System.out.println("destroyed redis pool");
        logger.info("destroyed redis pool");
    }

    protected void beforeInitialized(){

    }

    protected void afterInitialized(){
        redisInitialized();
    }

    protected void beforeDestroyed(){

    }

    protected void afterDestroyed(){
        redisDestroyed();
    }

}
