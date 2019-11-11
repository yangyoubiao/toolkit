package com.yangyoubiao.www.loadProperty.test;

import com.yangyoubiao.www.loadProperty.PropertyUtil;

/**
 * XXX配置文件
 */
public class XXXPropertyUtil {

	private static PropertyUtil propertyUtil = PropertyUtil.getInstance();

	static {
		propertyUtil.loadProps("XXX.properties");
	}

	public static String getProperty(String key) {
		return propertyUtil.getProperty(key);
	}

	/**
	 * 获取配置信息
	 * 
	 * @param key          key值
	 * @param defaultValue 默认值
	 */
	public static String getProperty(String key, String defaultValue) {
		return propertyUtil.getProperty(key, defaultValue);
	}

	public static void setProperty(String key, String value) {
		propertyUtil.setProperty(key, value);
	}

	public static void main(String[] args) {
		XXXPropertyUtil.getProperty("yyb");
	}
}
