package org.fastutil.mainland.util.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 性能监控<br/>
 * 默认切入点配置：execution(* org.fastutil..controller..*(..)) or execution(* org.fastutil..action..*(..)) or execution(* org.fastutil..performance..*(..))<br/>
 * 具体类需实现checkSwitch()方法，false:不打开监控，true:打开监控
 *
 * @author fastutil
 */
public abstract class AbstractPerformanceAOP {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractPerformanceAOP.class);

    @Pointcut(value = "execution(* org.fastutil..controller..*(..)) or execution(* org.fastutil..action..*(..)) or execution(* org.fastutil..performance..*(..))")
    protected void pointCutMethod() {
    }

    abstract public boolean checkSwitch();

    @Around(value = "pointCutMethod()")
    protected void around(ProceedingJoinPoint point) {
        if (checkSwitch()) {
            Object target = point.getTarget();// 拦截的实体类
            String className = target.getClass().getName();
            String methodName = point.getSignature().getName();// 拦截的方法名称
            // Object[] args = point.getArgs();// 拦截的方法参数
            // Class<?>[] parameterTypes = ((MethodSignature)
            // point.getSignature())
            // .getMethod().getParameterTypes();// 拦截的方法参数类型
            //
            // Method m = null;
            //
            // try {
            // m = target.getClass().getMethod(methodName, parameterTypes);//
            // 通过反射获得拦截的method
            // m.invoke(methodName, args);
            // } catch (NoSuchMethodException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (SecurityException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IllegalAccessException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IllegalArgumentException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (InvocationTargetException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            long time = System.currentTimeMillis();
            try {
                point.proceed();
            } catch (Throwable e) {
                logger.error("Throwable Message:" + e.getMessage(), e);
            }

            logger.info(new StringBuilder(200).append("currentThread:").append(Thread.currentThread().toString())
                    .append(" class:").append(className).append(" method:").append(methodName).append(" time(ms):")
                    .append((System.currentTimeMillis() - time)).toString());
        } else {
            try {
                point.proceed();
            } catch (Throwable e) {
                logger.error("Throwable Message:" + e.getMessage(), e);
            }
        }
    }

    // @Before(value = "pointCutMethod()")
    // public void begin(JoinPoint point) {
    // String configValue=PropUtil.getString("openPerformanceMonitor");
    // isFlush =
    // "true".equals(configValue)?true:("false".equals(configValue)?false:open);
    // if (isFlush) {
    // time = System.currentTimeMillis();
    // Object target = point.getTarget();// 拦截的实体类
    // className = target.getClass().getName();
    // methodName = point.getSignature().getName();// 拦截的方法名称
    // // Object[] args = point.getArgs();// 拦截的方法参数
    // // Class<?>[] parameterTypes = ((MethodSignature)
    // // point.getSignature())
    // // .getMethod().getParameterTypes();// 拦截的方法参数类型
    // //
    // // Method m = null;
    // //
    // // try {
    // // m = target.getClass().getMethod(methodName, parameterTypes);//
    // // 通过反射获得拦截的method
    // // m.invoke(methodName, args);
    // // } catch (NoSuchMethodException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (SecurityException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (IllegalAccessException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (IllegalArgumentException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (InvocationTargetException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // }
    // }
    //
    // @After(value = "pointCutMethod()")
    // public void end() {
    // if (isFlush) {
    // logger.info(new StringBuilder(200).append("currentThread:")
    // .append(Thread.currentThread().toString())
    // .append(" class:").append(className).append(" method:")
    // .append(methodName).append(" time(ms):")
    // .append((System.currentTimeMillis() - time)).toString());
    // }
    // }
}
