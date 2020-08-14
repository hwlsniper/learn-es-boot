package com.learn.es.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.learn.es.SpringBootExCrawlerApplicationTests;
import com.learn.es.service.impl.SpiderService;
import com.learn.es.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.test.TestSpider
 * @description 爬取数据
 * @date 2020/8/5 18:02
 */
@Slf4j
public class TestSpider extends SpringBootExCrawlerApplicationTests {

	@Test
	public void spider() {
		String url = "https://item.jd.com/65590733400.html";
		String skuId = Pattern.compile("[^0-9]").matcher(url).replaceAll("").trim();
		String sendRecvGet = HttpClientUtils.sendGet("http://p.3.cn/prices/get","skuid="+skuId);
		log.info("数据：{}", sendRecvGet);
		JSONArray dataArray = JSON.parseArray(sendRecvGet);
		JSONObject dataJsonObj = dataArray.getJSONObject(0);
		//提取价格
		Object priceStr = dataJsonObj.get("p");
		System.out.println("价格："+priceStr);
	}

	@Test
	public void replaceText() {
		String shopName = "杉城京东自营旗舰店";
		shopName = shopName.replace("京东", "");
		log.info(shopName);
	}
}
