package com.yangyoubiao.www.http;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {

	// 请求和传输超时时间
	public static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(20000).setConnectTimeout(20000)
			.build();

	public static String doGet(String url, Map<String, String> param) throws Exception {
		// 创建Httpclient对象
		CloseableHttpClient httpclient = HttpClients.createDefault();

		String resultString = "";
		CloseableHttpResponse response = null;
		try {
			// 创建uri
			URIBuilder builder = new URIBuilder(url);
			if (param != null) {
				for (String key : param.keySet()) {
					builder.addParameter(key, param.get(key));
				}
			}
			URI uri = builder.build();

			// 创建http GET请求
			HttpGet httpGet = new HttpGet(uri);

//			httpGet.setConfig(requestConfig);

			// 执行请求
			response = httpclient.execute(httpGet);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
			} else {
				throw new Exception("服务接口无法调用");
			}
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}

	public static String doGet(String url) throws Exception {
		return doGet(url, null);
	}

	public static String doPost(String url, Map<String, String> param) throws Exception {
		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
//			httpPost.setConfig(requestConfig);

			// 创建参数列表
			if (param != null) {
				List<NameValuePair> paramList = new ArrayList<>();
				for (String key : param.keySet()) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}
				// 模拟表单
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
				httpPost.setEntity(entity);
			}
			// 执行http请求
			response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
			} else {
				throw new Exception("服务接口无法调用");
			}
		} finally {

			if (response != null) {
				try {
					response.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		}
		return resultString;
	}

	public static String doPost(String url) throws Exception {
		return doPost(url, null);
	}

	public static String doPostJson(String url, String json) throws Exception {
		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);

//			httpPost.setConfig(requestConfig);

			// 创建请求内容
			StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			// 执行http请求
			response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
			} else {
				throw new Exception(response.getStatusLine().getStatusCode() + "服务接口无法调用");
			}
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		return resultString;
	}

	/**
	 * 上传文件
	 * 
	 * @param url
	 * @param file
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String doPostUpload(String url, File file, Map<String, String> param) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);// 创建httpPost
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
		httpPost.setConfig(requestConfig);
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		for (String key : param.keySet()) {
			multipartEntityBuilder.addTextBody(key, param.get(key));
		}
		multipartEntityBuilder.addBinaryBody("file", file, ContentType.create("multipart/form-data; charset=utf-8"),
				file.getName());
		HttpEntity httpEntity = multipartEntityBuilder.build();
		httpPost.setEntity(httpEntity);
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			response = httpclient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
			} else {
				throw new Exception("服务接口无法调用");
			}
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}

	public static void main(String[] args) throws Exception {

		/*
		 * DataSourceConfigDto dto = new DataSourceConfigDto();
		 * dto.setConnectType("wew"); dto.setDataName("sds"); String json =
		 * JSONArray.toJSONString(dto);
		 * HttpClientUtil.doPostJson("http://localhost:8080/global-task-app/test/test",
		 * json);
		 */
		/*
		 * Map<String,String> map = Maps.newHashMap(); map.put("ss", "cessss");
		 * HttpClientUtil.doGet("http://localhost:8080/global-task-app/test/get", map);
		 */

		Map<String, String> map = new HashMap<String, String>();
		map.put("server", "23121");
		map.put("root", "root");
		File file = new File("C:\\Users\\Times\\Desktop\\DATA_SOURCE_CONFIG.txt");
		HttpClientUtil.doPostUpload("http://localhost:8080/dataProcessService/hadoop/upload", file, map);
	}
}
