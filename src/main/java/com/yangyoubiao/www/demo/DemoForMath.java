package com.yangyoubiao.www.demo;

import java.util.Random;

/**
 * 关于一些数学公式的DEMO
 */
public class DemoForMath {

	public static void main(String[] args) {
		randomValue();
	}

	/**
	 * 生成随机整数
	 */
	public static void randomValue() {
		// [0-10]的随机整数
		System.out.println(new Random().nextInt(11));
		// 4位随机数
		System.out.println(String.format("%04d", new Random().nextInt(10000)));
		System.out.println(Math.round(Math.random() * 10000));
		System.out.println(Math.random());// 0.0---1.0
		System.out.println((int) (Math.random() * 10000));
	}
}
