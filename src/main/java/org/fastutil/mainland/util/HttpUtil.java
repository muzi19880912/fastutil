package org.fastutil.mainland.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * http请求
 * 
 * @author Administrator
 *
 */
public class HttpUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	public static final String DEFAULT_CHARSET = "UTF-8";
	public static final String POST = "POST";
	public static final String GET = "GET";

	/**
	 * GET获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址 <br/>
	 *            编码格式 默认UTF-8
	 * @return URL的返回内容
	 */
	public static final String getUrlReturnValue(String urlAddress) {
		return getUrlReturnValue(urlAddress, DEFAULT_CHARSET);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式 </br/>
	 *            请求方式:默认GET
	 * @return URL的返回内容
	 */
	public static final String getUrlReturnValue(String urlAddress, String charset) {
		return getUrlReturnValue(urlAddress, charset, GET);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @return URL的返回内容
	 */
	public static final String getUrlReturnValue(String urlAddress, String charset, String method) {
		return getUrlReturnValue(urlAddress, charset, method, null);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @param paramsMap
	 *            参数值
	 * @return URL的返回内容
	 */
	public static final String getUrlReturnValue(String urlAddress, String charset, String method,
			Map<String, String> paramsMap) {
		return getUrlReturnValue(urlAddress, charset, method, paramsMap, null);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @param paramsMap
	 *            参数值
	 * @param requestPropertiesMap
	 *            requestProperty值
	 * @return URL的返回内容
	 */
	public static final String getUrlReturnValue(String urlAddress, String charset, String method,
			Map<String, String> paramsMap, Map<String, String> requestPropertiesMap) {
		return getUrlReturnValue(urlAddress, charset, method, paramsMap, null, requestPropertiesMap);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @param paramsMap
	 *            参数值
	 * @param paramsKeyList
	 *            参数名称（有序排列）
	 * @param requestPropertiesMap
	 *            requestProperty值
	 * @return URL的返回内容
	 */
	public static final String getUrlReturnValue(String urlAddress, String charset, String method,
			Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap) {
		if (urlAddress == null) {
			throw new IllegalArgumentException("param 'urlAddress' is required");
		}
		if (urlAddress.startsWith("https:")) {
			return HttpsUtil.getUrlReturnValue(urlAddress, charset, method, paramsMap, paramsKeyList,
					requestPropertiesMap);
		}
		HttpURLConnection conn = null;
		int tempCode = -1;
		try {
			URL url = null;
			int index = urlAddress.indexOf("?");
			StringBuilder paramBuilder = null;
			if (index != -1 && urlAddress.length() > index + 1) {
				paramBuilder = new StringBuilder("&");
				if (urlAddress.indexOf("=") == -1) {
					String paramStr = CoderUtil.decode(urlAddress.substring(index + 1), charset);
					String[] params = StringUtils.isNotEmpty(paramStr)
							? paramStr.split("&(?=[a-zA-Z_]{1}[\\w]*\\=[\\s\\S]*(?=&|$))") : new String[0];
					for (String param : params) {
						int idx = param.indexOf("=");
						if (idx != -1)
							paramBuilder.append("&").append(param.substring(0, idx)).append("=")
									.append(CoderUtil.encode(param.substring(idx + 1), charset));
					}
					if (paramBuilder.length() > 1) {
						paramBuilder.deleteCharAt(0);
					}
				} else {
					paramBuilder.append(urlAddress.substring(index + 1));
				}
				urlAddress = urlAddress.substring(0, index);
			}
			if (paramsMap != null && paramsMap.size() > 0) {
				if (paramBuilder == null) {
					paramBuilder = new StringBuilder();
				}
				if (paramsKeyList != null && paramsKeyList.size() == paramsMap.size()) {
					for (String paramKey : paramsKeyList) {
						String tempValue = paramsMap.get(paramKey);
						if (tempValue == null) {
							tempValue = "";
						}
						paramBuilder.append("&").append(paramKey).append("=")
								.append(CoderUtil.encode(tempValue, charset));
					}
				} else {
					for (Entry<String, String> keyValue : paramsMap.entrySet()) {
						String tempValue = keyValue.getValue();
						if (tempValue == null) {
							tempValue = "";
						}
						paramBuilder.append("&").append(keyValue.getKey()).append("=")
								.append(CoderUtil.encode(tempValue, charset));
					}
				}
			}
			if (POST.equalsIgnoreCase(method)) {
				url = new URL(urlAddress);
				conn = (HttpURLConnection) url.openConnection();
				if (requestPropertiesMap != null) {
					for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
						String tempValue = keyValue.getValue();
						if (tempValue == null) {
							tempValue = "";
						}
						conn.addRequestProperty(keyValue.getKey(), tempValue);
					}
					logger.debug("requestProperties:" + requestPropertiesMap);
				}
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setConnectTimeout(60000);// 设置连接超时
				// 如果在建立连接之前超时期满，则会引发一个
				// java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
				conn.setReadTimeout(60000);// 设置读取超时
				conn.setRequestMethod(POST);
				if (paramBuilder != null) {
					while (paramBuilder.indexOf("&") == 0) {
						paramBuilder.deleteCharAt(0);
					}
					String temp = paramBuilder.toString();
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
					conn.connect();
					OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), charset);
					osw.write(temp);
					osw.flush();
					osw.close();
					logger.debug(temp);
				} else {
					conn.connect();
				}
			} else {
				if (paramBuilder != null) {
					while (paramBuilder.indexOf("&") == 0) {
						paramBuilder.deleteCharAt(0);
					}
					paramBuilder.insert(0, "?");
					paramBuilder.insert(0, urlAddress);
					urlAddress = paramBuilder.toString();
				}

				url = new URL(urlAddress);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(GET);

				if (requestPropertiesMap != null) {
					for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
						String tempValue = keyValue.getValue();
						if (tempValue == null) {
							tempValue = "";
						}
						conn.addRequestProperty(keyValue.getKey(), tempValue);
					}
					logger.debug("requestProperties:" + requestPropertiesMap);
				}
				conn.connect();
			}
			logger.debug(urlAddress);
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 1024);
			int length = -1;
			StringBuilder result = new StringBuilder();
			byte[] buf = new byte[1024];
			while ((length = bis.read(buf)) != -1) {
				result.append(new String(buf, 0, length, charset));
			}
			tempCode = conn.getResponseCode();
			bis.close();
			bis = null;
			buf = null;
			return result.toString();
		} catch (Exception e) {
			logger.error(new StringBuilder().append(urlAddress).append(" Code:").append(tempCode)
					.append(" getUrlReturnValue Exception>>>").append(e.getMessage()).toString(), e);
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Exception e) {
				}
				conn = null;
			}
		}
	}

	/**
	 * GET获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址 <br/>
	 *            编码格式 默认UTF-8
	 * @return status code from an HTTP response message
	 */
	public static final int getUrlAccessCode(String urlAddress) {
		return getUrlAccessCode(urlAddress, DEFAULT_CHARSET);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式 </br/>
	 *            请求方式:默认GET
	 * @return status code from an HTTP response message
	 */
	public static final int getUrlAccessCode(String urlAddress, String charset) {
		return getUrlAccessCode(urlAddress, charset, GET);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @return status code from an HTTP response message
	 */
	public static final int getUrlAccessCode(String urlAddress, String charset, String method) {
		return getUrlAccessCode(urlAddress, charset, method, null);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @param paramsMap
	 *            参数值
	 * @return status code from an HTTP response message
	 */
	public static final int getUrlAccessCode(String urlAddress, String charset, String method,
			Map<String, String> paramsMap) {
		return getUrlAccessCode(urlAddress, charset, method, paramsMap, null);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @param paramsMap
	 *            参数值
	 * @param requestPropertiesMap
	 *            requestProperty值
	 * @return URL的返回内容
	 */
	public static final int getUrlAccessCode(String urlAddress, String charset, String method,
			Map<String, String> paramsMap, Map<String, String> requestPropertiesMap) {
		return getUrlAccessCode(urlAddress, charset, method, paramsMap, null, requestPropertiesMap);
	}

	/**
	 * 获取访问地址的返回值
	 * 
	 * @param urlAddress
	 *            URL地址
	 * @param charset
	 *            编码格式
	 * @param method
	 *            GET,POST
	 * @param paramsMap
	 *            参数值
	 * @param paramsKeyList
	 *            参数名称（有序排列）
	 * @param requestPropertiesMap
	 *            requestProperty值
	 * @return status code from an HTTP response message
	 */
	public static final int getUrlAccessCode(String urlAddress, String charset, String method,
			Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap) {
		if (urlAddress == null) {
			throw new IllegalArgumentException("param 'urlAddress' is required");
		}
		if (urlAddress.startsWith("https:")) {
			return HttpsUtil.getUrlAccessCode(urlAddress, charset, method, paramsMap, paramsKeyList,
					requestPropertiesMap);
		}
		HttpURLConnection conn = null;
		int tempCode = -1;
		try {
			URL url = null;
			int index = urlAddress.indexOf("?");
			StringBuilder paramBuilder = null;
			if (index != -1 && urlAddress.length() > index + 1) {
				paramBuilder = new StringBuilder("&");
				if (urlAddress.indexOf("=") == -1) {
					String paramStr = CoderUtil.decode(urlAddress.substring(index + 1), charset);
					String[] params = StringUtils.isNotEmpty(paramStr)
							? paramStr.split("&(?=[a-zA-Z_]{1}[\\w]*\\=[\\s\\S]*(?=&|$))") : new String[0];
					for (String param : params) {
						int idx = param.indexOf("=");
						if (idx != -1)
							paramBuilder.append("&").append(param.substring(0, idx)).append("=")
									.append(CoderUtil.encode(param.substring(idx + 1), charset));
					}
					if (paramBuilder.length() > 1) {
						paramBuilder.deleteCharAt(0);
					}
				} else {
					paramBuilder.append(urlAddress.substring(index + 1));
				}
				urlAddress = urlAddress.substring(0, index);
			}
			if (paramsMap != null && paramsMap.size() > 0) {
				if (paramBuilder == null) {
					paramBuilder = new StringBuilder();
				}
				if (paramsKeyList != null && paramsKeyList.size() == paramsMap.size()) {
					for (String paramKey : paramsKeyList) {
						String tempValue = paramsMap.get(paramKey);
						if (tempValue == null) {
							tempValue = "";
						}
						paramBuilder.append("&").append(paramKey).append("=")
								.append(CoderUtil.encode(tempValue, charset));
					}
				} else {
					for (Entry<String, String> keyValue : paramsMap.entrySet()) {
						String tempValue = keyValue.getValue();
						if (tempValue == null) {
							tempValue = "";
						}
						paramBuilder.append("&").append(keyValue.getKey()).append("=")
								.append(CoderUtil.encode(tempValue, charset));
					}
				}
			}
			if (POST.equalsIgnoreCase(method)) {
				url = new URL(urlAddress);
				conn = (HttpURLConnection) url.openConnection();

				if (requestPropertiesMap != null) {
					for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
						String tempValue = keyValue.getValue();
						if (tempValue == null) {
							tempValue = "";
						}
						conn.addRequestProperty(keyValue.getKey(), tempValue);
					}
					logger.debug("requestProperties:" + requestPropertiesMap);
				}
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setConnectTimeout(60000);// 设置连接超时
				// 如果在建立连接之前超时期满，则会引发一个
				// java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
				conn.setReadTimeout(60000);// 设置读取超时
				conn.setRequestMethod(POST);
				if (paramBuilder != null) {
					while (paramBuilder.indexOf("&") == 0) {
						paramBuilder.deleteCharAt(0);
					}
					String temp = paramBuilder.toString();
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
					conn.connect();
					OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), charset);
					osw.write(temp);
					osw.flush();
					osw.close();
					logger.debug(temp);
				} else {
					conn.connect();
				}
			} else {
				if (paramBuilder != null) {
					while (paramBuilder.indexOf("&") == 0) {
						paramBuilder.deleteCharAt(0);
					}
					paramBuilder.insert(0, "?");
					paramBuilder.insert(0, urlAddress);
					urlAddress = paramBuilder.toString();
				}

				url = new URL(urlAddress);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(GET);

				if (requestPropertiesMap != null) {
					for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
						String tempValue = keyValue.getValue();
						if (tempValue == null) {
							tempValue = "";
						}
						conn.addRequestProperty(keyValue.getKey(), tempValue);
					}
					logger.debug("requestProperties:" + requestPropertiesMap);
				}
				conn.connect();
			}

			logger.debug(urlAddress);
			tempCode = conn.getResponseCode();
			return tempCode;
		} catch (Exception e) {
			logger.error(new StringBuilder().append(urlAddress).append(" Code:").append(tempCode)
					.append(" getUrlAccessCode Exception>>>").append(e.getMessage()).toString(), e);
			return -1;
		} finally {
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Exception e) {
				}
				conn = null;
			}
		}
	}
}
