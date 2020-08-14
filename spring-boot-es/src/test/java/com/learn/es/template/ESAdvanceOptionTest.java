package com.learn.es.template;

import com.learn.es.SpringBootEsApplicationTests;
import com.learn.es.constants.ESConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import java.io.IOException;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.template.ESAdvanceOptionTest
 * @description ES 进阶版操作
 * @date 2020/8/7 10:38
 */
@Slf4j
public class ESAdvanceOptionTest {

	/**
	 * 获取客户端
	 * @return
	 */
	private static RestHighLevelClient getClient() {
		/*RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(
						new HttpHost("node01", 9200, "http"),
						new HttpHost("node02", 9200, "http"),
						new HttpHost("node03", 9200, "http")));*/
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(
						new HttpHost("localhost", 9200, "http")));
		return client;
	}

	// https://blog.csdn.net/u014646662/article/details/100098851
	// https://blog.csdn.net/u014646662/article/details/96853830
	// https://blog.csdn.net/wojiushiwo987/category_9266239.html

	/**
	 * matchAll查询, 匹配所有文档
	 */
	public static void matchAllQuery() {
		RestHighLevelClient client = getClient();

		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_GOODS_INFO);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(10);
		searchSourceBuilder.sort(SortBuilders.fieldSort("goodsPrice").order(SortOrder.DESC));

		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] hits = response.getHits().getHits();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * match查询, 匹配查询, 会对查询的内容分词，区别于 term
	 */
	public static void matchQuery() {
		RestHighLevelClient client = getClient();

		// 这里可以不指定索引，也可以指定多个
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_GOODS_INFO);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("goodsName", "拼图");
		searchSourceBuilder.query(matchQueryBuilder);

		// 模糊查询
		//matchQueryBuilder.fuzziness(Fuzziness.AUTO);
		// 前缀查询的长度
		//matchQueryBuilder.prefixLength(3);
		// max expansion 选项，用来控制模糊查询
		//matchQueryBuilder.maxExpansions(10);

		searchSourceBuilder.from(0);
		searchSourceBuilder.size(20);
		// 按评分排序
		searchSourceBuilder.sort("_score");
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 精准匹配，不对搜索内容进行分词
	 */
	public static void termQuery() {
		RestHighLevelClient client = getClient();

		// 这里可以不指定索引，也可以指定多个
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_GOODS_INFO);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("goodsName", "拼图"));
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(20);
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}

	}

	/**
	 * 聚合查询
	 */
	public static void aggregation() {
		RestHighLevelClient client = getClient();

		// 这里可以不指定索引，也可以指定多个
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_GOODS_INFO);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("avg_price").field("price");
		MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min("min_price").field("price");
		MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("max_price").field("price");

		TermsAggregationBuilder group = AggregationBuilders.terms("group_shop_name").field("shopName");
		group.subAggregation(avgAggregationBuilder).subAggregation(minAggregationBuilder).subAggregation(maxAggregationBuilder);
		searchSourceBuilder.profile(true);
		searchSourceBuilder.aggregation(group);
		searchRequest.source(searchSourceBuilder);
		try {
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			Aggregations aggregations = searchResponse.getAggregations();
			Terms terms = aggregations.get("group_shop_name");
			terms.getBuckets().forEach(bucket -> {
				bucket.getAggregations().asMap().forEach((k,v) -> {
					log.info(bucket.getKeyAsString() + " key:{}", k, "->{}", ((NumericMetricsAggregation.SingleValue)v).getValueAsString());
				});
			});

			String shopName = "乐立方（CubicFun）自营官方旗舰店";
			Terms.Bucket bucket = terms.getBucketByKey(shopName);
			NumericMetricsAggregation.SingleValue avgPrice = bucket.getAggregations().get("avg_price");
			NumericMetricsAggregation.SingleValue minPrice = bucket.getAggregations().get("min_price");
			NumericMetricsAggregation.SingleValue maxPrice = bucket.getAggregations().get("max_price");

			log.info("平均价格:{}", avgPrice.value(), "最低价格:{}", minPrice.value(), "最高价格:{}", maxPrice.value());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 条件删除
	 */
	public static long deleteQuery() {
		RestHighLevelClient client = getClient();

		//参数为索引名，可以不指定，可以一个，可以多个
		DeleteByQueryRequest request = new DeleteByQueryRequest("hockey");
		// 更新时版本冲突
		request.setConflicts("proceed");
		// 设置查询条件，第一个参数是字段名，第二个参数是字段的值
		request.setQuery(new TermQueryBuilder("first","Sam"));
		// 处理文档的最大数量。默认值为-1，表示处理所有文档。
		request.setMaxDocs(10);
		// 用于控制每批处理的文档数量的滚动大小
		request.setBatchSize(1000);
		// 此任务应划分的片数。默认值为1，表示该任务没有分割为子任务。并行度
		request.setSlices(2);
		// 使用滚动参数来控制“搜索上下文”存活的时间
		request.setScroll(TimeValue.timeValueMinutes(10));
		// 超时
		request.setTimeout(TimeValue.timeValueMinutes(2));
		// 刷新索引
		request.setRefresh(true);

		try {
			BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
			return response.getStatus().getUpdated();
		} catch(IOException ex) {

		}
		return -1L;
	}

	/**
	 * 关闭客户端
	 * @param client
	 */
	private static void close(RestHighLevelClient client) {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
