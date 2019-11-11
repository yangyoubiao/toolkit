package com.yangyoubiao.www.util;

/**
 * 关于处理excel中的过程中一些公共的方法
 */
public class ExcelUtil {
	/**
	 * excel列号转数字，下标从1开始<br>
	 * excel的坐标表示如下A1、AA1 ,这里的A、AA为列号
	 * 
	 * @param colStr 列号：例如AA
	 * @param length 列号的长度
	 * @return
	 */
	public static int excelColStrToNum(String colStr, int length) {
		int num = 0;
		int result = 0;
		for (int i = 0; i < length; i++) {
			char ch = colStr.charAt(length - i - 1);
			num = (int) (ch - 'A' + 1);
			num *= Math.pow(26, i);
			result += num;
		}
		return result;
	}

	/**
	 * excel列号转数字，下标从1开始<br>
	 * excel的坐标表示如下A1、AA1 ,这里的A、AA为列号
	 * 
	 * @param r excel的坐标：例如AA1
	 * @return
	 */
	public static int excelColStrToNum(String r) {
		String colStr = r.replaceAll("\\d", "");
		int length = colStr.length();
		int num = 0;
		int result = 0;
		for (int i = 0; i < length; i++) {
			char ch = colStr.charAt(length - i - 1);
			num = (int) (ch - 'A' + 1);
			num *= Math.pow(26, i);
			result += num;
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(excelColStrToNum("AA"));
	}

}
