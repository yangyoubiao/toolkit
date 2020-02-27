package com.yangyoubiao.www.dataX;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.datax.core.Engine;

public class Demo {
	public static void start(String dataxPath, String jsonPath) throws Throwable {
		System.setProperty("datax.home", dataxPath);
		System.setProperty("now", new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS").format(new Date()));// 替换job中的占位符
		// String[] datxArgs = {"-job", dataxPath + "/job/text.json", "-mode",
		// "standalone", "-jobid", "-1"};
		String[] datxArgs = { "-job", jsonPath, "-mode", "standalone", "-jobid", "-1" };
		Engine.entry(datxArgs);
		Engine.getResult();
	}

}
