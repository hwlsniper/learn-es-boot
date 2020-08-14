package com.learn.es.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.learn.es.constant.ElasticsearchConstant;
import com.learn.es.entity.GoodsInfo;
import com.learn.es.service.GoodsService;
import com.learn.es.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ClientInfoStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.SpiderService
 * @description 爬虫服务
 * @date 2020/8/5 17:25
 */
@Slf4j
@Service
public class SpiderService {

	@Autowired
	private GoodsService goodsService;

	private static String HTTPS_PROTOCOL = "https:";

	public void spiderData(String url, Map<String, String> params) {
		Map<String, String> headers = new HashMap<>();
		headers.put("accept", "*/*");
		headers.put("connection", "Keep-Alive");
		headers.put("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");

		String html = HttpClientUtils.sendGet(url, headers, params);
		if(!StringUtils.isBlank(html)) {
			List<GoodsInfo> goodsInfos = parseHtml(html);
			goodsService.insert(ElasticsearchConstant.INDEX_NAME, goodsInfos);
		}
	}

	/**
	 * 解析html
	 * @param html
	 */
	private List<GoodsInfo> parseHtml(String html) {
		//商品集合
		List<GoodsInfo> goods = Lists.newArrayList();
		/**
		 * 获取dom并解析
		 */
		Document document = Jsoup.parse(html);

		Elements elements = document.
				select("ul[class=gl-warp clearfix]").select("li[class=gl-item]");
		int index = 0;
		for(Element element : elements) {
			String goodsId = element.attr("data-sku");
			String goodsName = element.select("div[class=p-name p-name-type-2]").select("em").text();
			String goodsPrice = element.select("div[class=p-price]").select("strong").select("i").text();
			String imgUrl = HTTPS_PROTOCOL + element.select("div[class=p-img]").select("a").select("img").attr("src");
			String shopName = element.select("div[class=p-shop]").select("span").select("a").text();
			if(StrUtil.isNotBlank(shopName)) {
				shopName = shopName.replace("京东", "");
			}
			GoodsInfo goodsInfo = new GoodsInfo(goodsId, goodsName, imgUrl, Double.valueOf(goodsPrice), shopName);
			goods.add(goodsInfo);
			String jsonStr = JSON.toJSONString(goodsInfo);
			log.info("商品数据:{}", jsonStr);
			/*if(index ++ == 9) {
				break;
			}*/
		}
		return goods;
	}
}
