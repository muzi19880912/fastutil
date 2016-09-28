package org.fastutil.mainland.util;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性文件缓存工具类
 *
 * @author Administrator
 */
public final class PropertiesCacheUtil {
    private static final Logger log = LoggerFactory.getLogger(PropertiesCacheUtil.class);
    /**
     * 文件名Map
     */
    private static final Map<String, String> filesMap = new ConcurrentHashMap<String, String>();
    /**
     * 所有文件key-value键值Map
     */
    private static final Map<String, String> fileValuesMap = new ConcurrentHashMap<String, String>();
    /**
     * 单个文件key-value键值Map
     */
    private static final Map<String, Map<String, String>> singleFileValuesMap = new ConcurrentHashMap<String, Map<String, String>>();

    /**
     * 获取属性文件fileName中key对应的value值
     *
     * @param key
     * @param fileName <br/> auto is true
     * @return
     */
    public final static String getValue(String key, String fileName) {
        return getValue(key, fileName, true);
    }

    /**
     * 获取属性文件fileName中key对应的value值
     *
     * @param key
     * @param fileName
     * @param auto     if (auto){fileName = loadAreaFileName(fileName);}
     * @return
     */
    public final static String getValue(String key, String fileName, boolean auto) {
        if (auto) {
            fileName = loadAreaFileName(fileName);
        }

        String tempKey = getKey(key, fileName);
        String value = fileValuesMap.get(tempKey);
        if (value == null) {
            loadPropertiesFile(fileName);
            value = fileValuesMap.get(tempKey);
            if (value == null) {
                value = "";
            }
        }
        return value;
    }

    /**
     * 重新加载属性文件
     *
     * @param fileName <br/> auto is false
     * @return
     */
    protected final static String reLoadPropertiesFile(String fileName) {
        return reLoadPropertiesFile(fileName, false);
    }

    /**
     * 重新加载属性文件
     *
     * @param fileName
     * @param auto     if (auto){fileName = loadAreaFileName(fileName);}
     * @return
     */
    public final static String reLoadPropertiesFile(String fileName, boolean auto) {
        if (fileName == null || fileName.trim().length() == 0) {
            return "no";
        }

        if (auto) {
            fileName = loadAreaFileName(fileName);
        }

        String str = filesMap.get(fileName);
        if ("0".equals(str)) {
            return "loading";
        } else if ("1".equals(str)) {
            try {
                filesMap.put(fileName, "0");
                return loadPropertiesFile(fileName);
            } finally {
                filesMap.put(fileName, "1");
            }
        } else {
            return "no";
        }
    }


    /**
     * 是否包含此文件
     *
     * @param classFile <br/> auto is false
     * @return
     */
    protected final static boolean containFile(String classFile) {
        return containFile(classFile, false);
    }

    /**
     * 是否包含此文件
     *
     * @param classFile
     * @param auto      if (auto){classFile = loadAreaFileName(classFile);}
     * @return
     */
    public final static boolean containFile(String classFile, boolean auto) {
        if (auto) {
            classFile = loadAreaFileName(classFile);
        }
        return filesMap.containsKey(classFile);
    }

    /**
     * 获取文件的所有键值对
     *
     * @param fileName <br/> auto is true
     * @return
     */
    public final static Map<String, String> loadAllValues(String fileName) {
        return loadAllValues(fileName, true);
    }

    /**
     * 获取文件的所有键值对
     *
     * @param fileName
     * @param auto     if (auto){fileName = loadAreaFileName(fileName);}
     * @return
     */
    public final static Map<String, String> loadAllValues(String fileName, boolean auto) {
        if (CommonUtil.objectIsNull(fileName)) {
            return null;
        } else {
            if (auto) {
                fileName = loadAreaFileName(fileName);
            }
            Map<String, String> temp = singleFileValuesMap.get(fileName);
            if (temp == null) {
                loadPropertiesFile(fileName);
                temp = singleFileValuesMap.get(fileName);
            }
            return temp;
        }
    }

    /**
     * 加载对应区域的文件名称
     *
     * @param fileName
     * @return
     */
    public final static String loadAreaFileName(String fileName) {
        if (fileName != null) {
            String area = PropertyConfigurer.getRegionSystemProperty();
            if (area != null) {
                area = new StringBuilder(area.length() + 2).append(area).append(File.separatorChar).toString();
                if (fileName.startsWith(area)) {
                    return fileName;
                } else {
                    return new StringBuilder(area.length() + fileName.length()).append(area).append(fileName)
                            .toString();
                }
            }
        }
        return fileName;
    }

    /**
     * 加载属性文件
     *
     * @param fileName
     * @return
     */
    private final static String loadPropertiesFile(String fileName) {

        if (fileName == null || fileName.trim().length() == 0) {
            return "no";
        }

        if ("1".equals(filesMap.get(fileName))) {
            return "ok";
        }

        String filePath = new StringBuilder(PropertiesFileLoader.getClassPath().length() + fileName.length()).append(PropertiesFileLoader.getClassPath()).append(fileName).toString();
        if (!new File(filePath).exists()) {
            log.error("file is not exists:{}", filePath);
            return "no";
        }

        synchronized (PropertiesCacheUtil.class) {

            if ("1".equals(filesMap.get(fileName))) {
                return "ok";
            }

            ArrayList<String> tempKeyList = new ArrayList<String>();
            Map<String, String> tempMap = new HashMap<String, String>();
            try {
                // 加载属性文件
                CompositeConfiguration propConfig = new CompositeConfiguration();
                // 改变Apache Configuration List默认分隔符，并禁用
                AbstractConfiguration.setDefaultListDelimiter('~');

                propConfig.setDelimiterParsingDisabled(true);
                // propConfig.clear();
                propConfig.addConfiguration(new PropertiesConfiguration(fileName));

                Iterator<String> it = propConfig.getKeys();
                while (it.hasNext()) {
                    String key = it.next();
                    if (CommonUtil.objectIsNull(key)) {
                        continue;
                    }
                    String value = propConfig.getString(key, "");

                    // 支持加密文件
                    if (CommonUtil.objectIsNotNull(value)) {
                        String temp=value;
                        try {
                            temp = SecretUtil.decrypt(value, null);
                        } catch (Throwable e) {
                        }
                        log.info("source:key={},value(before)={},value(after)={}", key, value, CommonUtil.objectIsNotNull(temp)?(value=temp):value);
                    }

                    key = key.trim();
                    String cacheKey = getKey(key, fileName);
                    tempKeyList.add(cacheKey);

                    if (value == null) {
                        value = "";
                    }
                    fileValuesMap.put(cacheKey, value);
                    tempMap.put(key, value);

                    String loadMsg = new StringBuilder().append("file:").append(fileName).append(" key:").append(key)
                            .append(" value:").append(value).toString();
                    System.out.println(loadMsg);
                    log.info(loadMsg);
                }

                singleFileValuesMap.put(fileName, tempMap);

                if (filesMap.get(fileName) == null) {
                    System.out.println("加载属性文件成功>>>classpath:" + fileName);
                    log.info("加载属性文件成功>>>classpath:" + fileName);
                } else {
                    // 该文件中不存在的key从fileValuesMap中删除
                    String commonKey = new StringBuilder().append(fileName).append("#?").toString();
                    Set<String> keySet = fileValuesMap.keySet();
                    for (String tempKey : keySet) {
                        if (tempKey != null && tempKey.startsWith(commonKey) && !tempKeyList.contains(tempKey)) {
                            fileValuesMap.remove(tempKey);
                        }
                    }
                    System.out.println("重新加载属性文件成功>>>classpath:" + fileName);
                    log.info("重新加载属性文件成功>>>classpath:" + fileName);
                }
            } catch (Throwable e) {
                log.error(fileName + " load fail:error", e);
                System.out.println(fileName + " load fail:error>" + e.getMessage());
            }
            // 文件加载标识
            filesMap.put(fileName, "1");
        }
        return "yes";
    }

    private final static String getKey(String key, String fileName) {
        return new StringBuilder(key.length() + fileName.length() + 2).append(fileName).append("#?").append(key)
                .toString();
    }

}
