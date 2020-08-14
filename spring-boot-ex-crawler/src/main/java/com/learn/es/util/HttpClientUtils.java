package com.learn.es.util;

import com.learn.es.constant.SysConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.util.HttpClientUtils
 * @description http client
 * @date 2020/8/5 17:02
 */
@Slf4j
public class HttpClientUtils {

	private final static String GET_METHOD = "GET";
	private final static String POST_METHOD = "POST";

	/**
	 * GET请求
	 *
	 * @param url
	 *            请求url
	 * @param headers
	 *            头部
	 * @param params
	 *            参数
	 * @return
	 */
	public static String sendGet(String url, Map<String, String> headers, Map<String, String> params) {
		// 创建HttpClient对象
		CloseableHttpClient client = HttpClients.createDefault();
		StringBuilder reqUrl = new StringBuilder(url);
		String result = "";
		/*
		 * 设置param参数
		 */
		if (params != null && params.size() > 0) {
			reqUrl.append("?");
			for (Map.Entry<String, String> param : params.entrySet()) {
				reqUrl.append(param.getKey() + "=" + param.getValue() + "&");
			}
			url = reqUrl.subSequence(0, reqUrl.length() - 1).toString();
		}
		log.debug("[url:" + url + ",method:" + GET_METHOD + "]");
		HttpGet httpGet = new HttpGet(url);
		/**
		 * 设置头部
		 */
		log.debug("Header\n");
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				httpGet.addHeader(header.getKey(), header.getValue());
				log.debug(header.getKey() + " : " + header.getValue());
			}
		}
		httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0");
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpGet);
			/**
			 * 请求成功
			 */
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity, SysConstant.DEFAULT_CHARSET);
			}
		} catch (IOException e) {
			log.error("网络请求出错，请检查原因");
		} finally {
			// 关闭资源
			try {
				if (response != null) {
					response.close();
				}
				client.close();
			} catch (IOException e) {
				log.error("网络关闭错误错，请检查原因");
			}
		}
		return result;
	}

	public static String sendGet(String url, String param) {
		String result = "";
		String urlName = url + "?" + param;
		try {
			URL realURL = new URL(urlName);
			URLConnection conn = realURL.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			conn.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += "\n" + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * POST请求
	 *
	 * @param url
	 *            请求url
	 * @param headers
	 *            头部
	 * @param params
	 *            参数
	 * @return
	 */
	public static String sendPost(String url, Map<String, String> headers, Map<String, String> params) {
		CloseableHttpClient client = HttpClients.createDefault();
		String result = "";
		HttpPost httpPost = new HttpPost(url);
		/**
		 * 设置参数
		 */
		if (params != null && params.size() > 0) {
			List<NameValuePair> paramList = new ArrayList<>();
			for (Map.Entry<String, String> param : params.entrySet()) {
				paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
			}
			log.debug("[url: " + url + ",method: " + POST_METHOD + "]");
			// 模拟表单提交
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, SysConstant.DEFAULT_CHARSET);
				httpPost.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				log.error("不支持的编码");
			}
			/**
			 * 设置头部
			 */
			if (headers != null && headers.size() > 0) {
				log.debug("Header\n");
				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpPost.addHeader(header.getKey(), header.getValue());
						log.debug(header.getKey() + " : " + header.getValue());
					}
				}
			}
			CloseableHttpResponse response = null;
			try {
				response = client.execute(httpPost);
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity, SysConstant.DEFAULT_CHARSET);
			} catch (IOException e) {
				log.error("网络请求出错，请检查原因");
			} finally {
				try {
					if (response != null) {
						response.close();
					}
					client.close();
				} catch (IOException e) {
					log.error("网络关闭错误");
				}
			}
		}
		return result;
	}
	/**
	 * post请求发送json
	 * @param url
	 * @param json
	 * @param headers
	 * @return
	 */
	public static String senPostJson(String url, String json, Map<String, String> headers) {
		CloseableHttpClient client = HttpClients.createDefault();
		String result = "";
		HttpPost httpPost = new HttpPost(url);
		StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
		httpPost.setEntity(stringEntity);
		log.debug("[url: " + url + ",method: " + POST_METHOD + ", json: " + json + "]");
		/**
		 * 设置头部
		 */
		if (headers != null && headers.size() > 0) {
			log.debug("Header\n");
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					httpPost.addHeader(header.getKey(), header.getValue());
					log.debug(header.getKey() + " : " + header.getValue());
				}
			}
		}
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity, SysConstant.DEFAULT_CHARSET);
		} catch (IOException e) {
			log.error("网络请求出错，请检查原因");
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				client.close();
			} catch (IOException e) {
				log.error("网络关闭错误");
			}
		}
		return result;
	}
}
