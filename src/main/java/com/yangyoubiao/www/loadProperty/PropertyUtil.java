package com.yangyoubiao.www.loadProperty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyUtil {
	private final Logger logger = Logger.getLogger(PropertyUtil.class);
	private Properties props;

	private PropertyUtil() {
	}

	/**
	 * 通过方法实例化一个配置，但是不是单例模式
	 */
	public static PropertyUtil getInstance() {
		return new PropertyUtil();
	}

	/**
	 * 加载从classes里加载配置文件
	 * 
	 * @param url 文件路径
	 */
	public void loadProps(String url) {
		logger.info("开始加载properties文件内容.......");
		props = new Properties();
		InputStream in = null;
		try {
			in = PropertyUtil.class.getClassLoader().getResourceAsStream(url);
			props.load(in);
		} catch (FileNotFoundException e) {
			logger.error(url + "文件未找到");
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(url + "文件流关闭出现异常");
			}
		}
		logger.info("加载properties文件内容完成...........");
		logger.info("properties文件内容：" + props);
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}
}
