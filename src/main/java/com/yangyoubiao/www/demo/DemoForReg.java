package com.yangyoubiao.www.demo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则的例子：
 */
public class DemoForReg {
	public static void main(String[] args) {
		findSubString();
	}

	/**
	 * 根据正则表达式，从指定字符串获取字串
	 */
	public static void findSubString() {
		String str = "SUBST_ID = 10307 OR SUBST_ID = 200000000 OR SUBST_ID = 10004 OR SUBST_ID = 10005 OR SUBST_ID = 4050 OR SUBST_ID = -1 OR SUBST_ID = 10061 OR SUBST_ID = 10006 OR SUBST_ID = 10002 OR SUBST_ID = 11149 OR SUBST_ID = 10003 OR SUBST_ID = 10922 OR SUBST_ID = 10317 OR SUBST_ID = 4174 OR SUBST_ID = 10061";
		String regex = "=\\s*'?(\\w+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		while (m.find()) {
			System.out.println(str.substring(m.start(1), m.end(1)));
		}
	}

}
