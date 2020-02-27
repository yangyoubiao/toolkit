package com.yangyoubiao.www.util;

import java.util.Map;
import java.util.Set;

/**
 * Collection的公共方法
 */
public class CollectionUtils {
	/**
	 * 打印map对象
	 */
	public static void printMap(Map<String, Object> map) {
		Set<String> set = map.keySet();
		for (String str : set) {
			System.out.println("key:" + str + "      value:" + map.get(str));
		}
	}
}
