package com.yangyoubiao.www.demo;

import java.util.HashMap;
import java.util.Map;

public class Test {

	public static void main(String[] args) throws Exception {
		for(int i=0;i<500;i++) {
			Map<String, String> map = new HashMap<String, String>();
			String url = "http://tw0urg.258.dos369.top/app/index.php?i=1&c=entry&rid=261&id=17808&do=vote&m=tyzm_diamondvote&latitude=0&longitude=0&verify=0";
			String result = com.yangyoubiao.www.http.HttpClientUtil.doPost(url, map);
			System.out.println(result);
			Thread.sleep(200);
			
		}
	}
}
