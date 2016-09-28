package org.fastutil.mainland.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 属性文件加载器<br/>
 * <font color=red> need to set the system property projectSystemPropertyRegion to
 * value <br/>
 * eg:projectSystemPropertyRegion=cn <br/>
 * default projectSystemPropertyRegion is null </font>
 *
 *
 */
public class PropertyConfigurer extends PropertyPlaceholderConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(PropertyConfigurer.class);

	private static final String CLASSPATH = "classpath:";
	private static final String SYSTEM_PROPERTY_REGION_KEY = "projectSystemPropertyRegion";
	private static final String SYSTEM_PROPERTY_REGION_VALUE = System.getProperty(SYSTEM_PROPERTY_REGION_KEY);

	private List<Properties> mpropList = new ArrayList<Properties>();
	protected String prop;
	protected String[] props;
	protected List<String> propList;

	public String getProp() {
		return prop;
	}

	public void setProp(String prop) {
		this.prop = prop;

		load(prop);
	}

	public String[] getProps() {
		return props;
	}

	public void setProps(String[] props) {
		this.props = props;

		if (props != null && props.length > 0) {
			for (String prop : props) {
				load(prop);
			}
		}
	}

	public List<String> getPropList() {
		return propList;
	}

	public void setPropList(List<String> propList) {
		this.propList = propList;

		if (propList != null && propList.size() > 0) {
			for (String prop : propList) {
				load(prop);
			}
		}
	}

	public static final String getRegionSystemProperty() {
		return SYSTEM_PROPERTY_REGION_VALUE;
	}

	@Override
	protected Properties mergeProperties() throws IOException {
		// TODO Auto-generated method stub
		Properties result = super.mergeProperties();
		if (this.mpropList.size() > 0) {
			for (Properties localProp : this.mpropList) {
				CollectionUtils.mergePropertiesIntoMap(localProp, result);
			}
			if (!this.localOverride) {
				// Load properties from file afterwards, to let those properties
				// override.
				loadProperties(result);
			}
		}

		return result;
	}

	protected void load(String path) {
		if (StringUtils.isNotBlank(path)) {
			if (path.startsWith(CLASSPATH)) {
				String classpath = PropertiesFileLoader.getClassPath();
				String area = getRegionSystemProperty();
				if (area != null) {
					classpath += area;
					classpath += File.separatorChar;
				}
				path = classpath + path.substring(CLASSPATH.length());

				logger.info("resources path:" + classpath);
				System.out.println("resources path:" + classpath);
			}
			File file = new File(path);
			if (file.exists()) {
				if (file.isFile()) {
					System.out.println("path is file:" + path);
					logger.info("path is file:" + path);
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(file);
						Properties p = new Properties();
						p.load(fis);
						mpropList.add(p);
					} catch (Exception e) {
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e1) {
							}
						}
					}
				} else {
					System.out.println("path is not file:" + path);
					logger.info("path is not file:" + path);
				}
			} else {
				System.out.println("path not exists:" + path);
				logger.info("path not exists:" + path);
			}
		}
	}
}
