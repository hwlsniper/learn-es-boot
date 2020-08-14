package com.learn.es.test;

import com.learn.es.SpringBootExCrawlerApplicationTests;
import com.learn.es.constant.ElasticsearchConstant;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.test.TestSearchGoods
 * @description RestHighLevelClient 高级用法
 * @date 2020/8/6 17:44
 */
@Slf4j
public class TestSearchGoods extends SpringBootExCrawlerApplicationTests {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 * 精准搜索
	 */
	public void accurateSeaarch() {

	}

	/**
	 * 滚动搜索
	 */
	@Test
	public void scrollSearch() {
		// 开启scroll查询,设置scroll过期时间为1min
		Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
		SearchRequest searchRequest = new SearchRequest(ElasticsearchConstant.INDEX_NAME);
		searchRequest.scroll(scroll);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(1000);
		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("goodsName", "拼图");
		searchSourceBuilder.query(matchQueryBuilder);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = null;

		Long startTime = System.currentTimeMillis();
		try {
			searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(null != searchResponse) {
			SearchHit[] hits = searchResponse.getHits().getHits();
			if(null != hits && hits.length > 0) {
				for(SearchHit searchHit : hits) {

				}
			}

			String scrollId = searchResponse.getScrollId();

			int page = 1;
			while(null != hits &&hits.length > 0) {
				SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
				searchScrollRequest.scrollId(scrollId);
				searchScrollRequest.scroll(TimeValue.timeValueMillis(1L));
				try {
					searchResponse = restHighLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
				} catch (IOException e) {
					e.printStackTrace();
				}
				scrollId = searchResponse.getScrollId();
				hits = searchResponse.getHits().getHits();
				if (hits != null && hits.length > 0) {
					page++;
					System.out.println("第"+page+"页");
					Arrays.asList(hits).stream().map(h -> h.getSourceAsString()).forEach(System.out::println);
				}
			}

			ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
			clearScrollRequest.addScrollId(scrollId);
			ClearScrollResponse clearScrollResponse;
			try {
				clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
				boolean succeeded = clearScrollResponse.isSucceeded();
				System.out.println("清除滚屏是否成功:" + succeeded);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			Long endTime = System.currentTimeMillis();
			log.info("耗时:{}", endTime - startTime);
			close(restHighLevelClient);
		}
	}

	/**
	 * 关闭客户端
	 *
	 * @param client
	 */
	private void close(RestHighLevelClient client) {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
