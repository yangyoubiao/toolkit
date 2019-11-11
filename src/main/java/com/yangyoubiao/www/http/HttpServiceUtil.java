package com.yangyoubiao.www.http;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 
 * 
 * @date 2018年10月26日 上午10:29:29
 * @author Ren Nanqing
 */
public class HttpServiceUtil {
	
	private static Logger log = LoggerFactory.getLogger(HttpServiceUtil.class);
	
	public static <T> T doPostJson(String url, Object requestObject, TypeReference<T> typeRef) throws Exception {
		printRequestMsg(url, RequestMethod.POST, requestObject);
		String responseJson = HttpClientUtil.doPostJson(url, JSON.toJSONString(requestObject, SerializerFeature.DisableCircularReferenceDetect));
		printResponseMsg(url, responseJson);
		return JSON.parseObject(responseJson, typeRef);
	}
	
	public static <T> T doPost(String url, TypeReference<T> typeRef) throws Exception {
		return doPost(url, null, typeRef);
	}
	
	public static <T> T doPost(String url, Map<String, String> param, TypeReference<T> typeRef) throws Exception {
		printRequestMsg(url, RequestMethod.POST, param);
		String responseJson = HttpClientUtil.doPost(url, param);
		printResponseMsg(url, responseJson);
		return JSON.parseObject(responseJson, typeRef);
	}

	public static <T> T doGet(String url, TypeReference<T> typeRef) throws Exception {
		return doGet(url, null, typeRef);
	}

	public static <T> T doGet(String url, Map<String, String> param, TypeReference<T> typeRef) throws Exception {
		printRequestMsg(url, RequestMethod.GET, param);
		String responseJson = HttpClientUtil.doGet(url, param);
		printResponseMsg(url, responseJson);
		return JSON.parseObject(responseJson, typeRef);
	}

	public static <T> T doPostUpload(String url, File file, Map<String, String> param, TypeReference<T> typeRef)
			throws Exception {
		printRequestMsg(url, RequestMethod.POST, param);
		String responseJson = HttpClientUtil.doPostUpload(url, file, param);
		printResponseMsg(url, responseJson);
		return JSON.parseObject(responseJson, typeRef);
	}

	/**
	 * 打印请求信息
	 * 
	 * @author Ren Nanqing
	 * @param url
	 * @param requestMethod
	 * @param requestObject
	 */
	public static void printRequestMsg(String url, RequestMethod requestMethod, Object... requestObject) {
		String requestContent = JSON.toJSONString(requestObject, SerializerFeature.DisableCircularReferenceDetect);
		String msg = String.format("Http service: %s [%s] - Request content: %s", url, requestMethod.name(),
				requestContent);
		// String msg = "打印日志";
		log.info(msg);
		System.out.println(msg);
	}

	public static void printResponseMsg(String url, Object... responseObject) {
		String responseContent = JSON.toJSONString(responseObject, SerializerFeature.DisableCircularReferenceDetect);
		String msg = String.format("Http service: %s - Response content: %s", url, responseContent);
		log.info(msg);
		System.out.println(msg);
	}

}
