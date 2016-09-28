package org.fastutil.mainland.util;

import com.efun.general.BeanHelper;
import com.efun.general.GeneralHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

public class Log4jConfigListener implements ServletContextListener {
	private final String LOG4J_LOCATION = "log4jLocation";
	private final String XML_FILE_EXTENSION = ".xml";
	private final String SERVER_ID = "server_id";
	private final String ROOT_PATH = "root_path";
	private final String LOG_SWITCH = "logSwitch";

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		String location = servletContext.getInitParameter(LOG4J_LOCATION);
		String logSwitch = servletContext.getInitParameter(LOG_SWITCH);
		String logFilePath = null;
		String classpath;
		String area = PropertyConfigurer.getRegionSystemProperty();
		if (area != null) {
			classpath = PropertiesFileLoader.getClassPath() + area + File.separatorChar;
		} else {
			classpath = PropertiesFileLoader.getClassPath();
		}

		System.out.println("system classpath:" + classpath);

		if (location == null || location.trim().length() == 0) {
			if (!new File(logFilePath = classpath + "log4j2.json").exists()) {
				if (!new File(logFilePath = classpath + "log4j2.xml").exists()) {
					if (!new File(logFilePath = classpath + "log4j2.properties").exists()) {
						if (!new File(logFilePath = classpath + "log4j.xml").exists()) {
							if (!new File(logFilePath = classpath + "log4j.properties").exists()) {
								logFilePath = null;
							}
						}
					}
				}
			}
		} else {
			if (!new File(logFilePath = classpath + location).exists()) {
				logFilePath = null;
			}
		}

		String serverId = getServerId();
		String system = GeneralHelper.isWindowsPlatform() ? "D:" : "";
		System.setProperty(ROOT_PATH, system);
		System.out.println(ROOT_PATH + "=" + system);
		System.setProperty(SERVER_ID, serverId);
		System.out.println(SERVER_ID + "=" + serverId);

		if (logFilePath != null) {
			try {
				if (logFilePath.endsWith("log4j2.xml") || logFilePath.endsWith("log4j2.json")
						|| logFilePath.endsWith("log4j2.properties")) {
					if (!"log4j2Sync".equals(logSwitch)) {
						System.setProperty("Log4jContextSelector",
								"org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
					}

					// Configurator.shutdown((LoggerContext)
					// LogManager.getContext());

					// ((LoggerContext)
					// LogManager.getContext(false)).setConfigLocation(new
					// File(logFilePath).toURI());
					// Object obj =
					// BeanHelper.getMethodByName(Class.forName("org.apache.logging.log4j.LogManager"),
					// "getContext", boolean.class).invoke(null, false);
					// BeanHelper.invokeMethod(obj,
					// BeanHelper.getMethodByName(obj.getClass(),
					// "setConfigLocation", URI.class),
					// new File(logFilePath).toURI());

					Object obj = BeanHelper.getMethodByName(Class.forName("org.apache.logging.log4j.LogManager"),
							"getContext", boolean.class).invoke(null, false);
					BeanHelper.invokeMethod(obj, BeanHelper.getMethodByName(obj.getClass(), "reconfigure"));

				} else if (logFilePath.endsWith(XML_FILE_EXTENSION)) {
					// DOMConfigurator.configure(logFilePath);
					BeanHelper.getMethodByName(Class.forName("org.apache.log4j.xml.DOMConfigurator"), "configure",
							String.class).invoke(null, logFilePath);
				} else {
					// PropertyConfigurator.configure(logFilePath);
					BeanHelper.getMethodByName(Class.forName("org.apache.log4j.PropertyConfigurator"), "configure",
							String.class).invoke(null, logFilePath);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("初始化log4j参数失败: " + e.getMessage());
			}
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	/**
	 * "sun.java.command":"com.caucho.server.resin.Resin --root-directory
	 * /usr/local/resin/ -conf
	 * /usr/local/resin/conf/vhost/analysis.efun.com.conf -socketwait 41798
	 * -server analysis01 restart"
	 */
	private static final String getServerId() {
		String serverid = "";
		String startCommand = System.getProperty("sun.java.command");
		if (startCommand != null) {
			String[] str = startCommand.split(" ");

			one: for (int i = 0; i < str.length; i++) {
				if ("-server".equalsIgnoreCase(str[i].trim())) {
					int j = i;
					while (++j < str.length && (serverid = str[j].trim()).length() > 0) {
						serverid = "." + serverid;
						break one;
					}
					break one;
				}
			}
		}
		return serverid;
	}

}