package com.learn.es.template;

import cn.hutool.json.JSONUtil;
import com.learn.es.SpringBootEsApplicationTests;
import com.learn.es.constants.ESConstant;
import com.learn.es.model.Person;
import com.learn.es.model.UseLogTwoDO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.template.TemplateTest
 * @description ES 相对复杂操作，由浅入深  ElasticsearchRestTemplate + RestHighLevelClient
 * @date 2020/8/4 11:05
 */
@Slf4j
public class TemplateTest extends SpringBootEsApplicationTests {

	@Autowired
	private ElasticsearchRestTemplate esTemplate;

	@Autowired
	private RestHighLevelClient highLevelClient;

	/**
	 * 创建索引的方法一
	 */
	@Test
	public void testCreateIndex() {
		// 创建索引，会根据Item类的@Document注解信息来创建
		//esTemplate.createIndex(Person.class); (已废除)

		// 这种创建 index 方法底层的实现其实是下面第二种方法
		IndexOperations indexOperations = esTemplate.indexOps(Person.class);
		if(!indexOperations.exists()) {
			indexOperations.create();
			log.info("Create successfully!");
		} else {
			log.info("Index has been created!");
		}

		// 配置映射，会根据Item类中的id、Field等字段来自动完成映射
		// esTemplate.putMapping(Person.class);(已废除)
	}

	/**
	 * 创建索引的方法二
	 */
	@Test
	public void testCreateIndex2() throws IOException {
		String indexName = "demo-use-log2";
		CreateIndexRequest request = new CreateIndexRequest(indexName);
		request.settings(getSetting());
		if(highLevelClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)) {
			CreateIndexResponse createIndexResponse = highLevelClient.indices().create(request, RequestOptions.DEFAULT);
			if(createIndexResponse.isAcknowledged()) {
				log.info("Create successfully!");
			}
		} else {
			log.info("Index has been created!");
		}
	}

	private static XContentBuilder getSetting() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		builder.field("index").startObject()
				.field("number_of_shards", 1)
				.field("number_of_replicas", 0)
				.field("refresh_interval", "30s").endObject();
		builder.endObject();
		return builder;
	}

	/**
	 * 添加文档
	 */
	@Test
	public void addDoc() {
		List<IndexQuery> queryList = new ArrayList<>();
		Long startTime = System.currentTimeMillis();
		for(int i = 400000000; i < 500000000; i++) {
			IndexQuery query = new IndexQuery();
			UseLogTwoDO useLogDO = UseLogTwoDO.builder().id(String.valueOf(i)).sortNo(i).result(String.format("我是%d号", i)).createTime(new Date()).build();
			query.setId(useLogDO.getId());
			query.setObject(useLogDO);
			queryList.add(query);
			if(i % 100000 == 0) {
				esTemplate.bulkIndex(queryList, esTemplate.getIndexCoordinatesFor(UseLogTwoDO.class));
				queryList.clear();
			}
		}
		//List<String> strings = esTemplate.bulkIndex(queryList, esTemplate.getIndexCoordinatesFor(UseLogTwoDO.class));
		Long endTime = System.currentTimeMillis();
		log.info("结果：{}", endTime - startTime);
	}

	/**
	 * 修改文档
	 */
	@Test
	public void modifyDoc() {
		Map<String, Object> param = new HashMap<>();
		param.put("result", "我是更新后的5号");
		//使用 painless 语言和前面的参数创建内嵌脚本
		UpdateQuery updateQuery = UpdateQuery.builder("5")
				.withParams(param)
				.withScript("ctx._source.result=params.result")
				.withLang("painless")
				.withRefresh(UpdateQuery.Refresh.True)
				.build();
		UpdateResponse response = esTemplate.update(updateQuery, esTemplate.getIndexCoordinatesFor(UseLogTwoDO.class));
		log.info("结果：{}", response.getResult());
	}

	/**
	 * 搜索
	 */
	@Test
	public void testQuery1() {
		BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
		queryBuilder.should(QueryBuilders.rangeQuery("sortNo").gt(5));
		queryBuilder.must(QueryBuilders.matchQuery("result", "更新"));
		NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder);
		// 深度分页，会在 shard 上创建一个 size + page(from) 大小的队列，ES 中默认这个队列的大小不可以超过 10000
		// 其实在分页查询中可以考虑 scroll 滚动分页的方式，队列的大小可以在 index 上进行设置 {"index":{"max_result_window":1000000}}
		nativeSearchQuery.setPageable(PageRequest.of(0, 10000));
		Long startTime = System.currentTimeMillis();
		SearchHits<UseLogTwoDO> search = esTemplate.search(nativeSearchQuery, UseLogTwoDO.class);
		List<SearchHit<UseLogTwoDO>> searchHits = search.getSearchHits();
		Long endTime = System.currentTimeMillis();
		log.info("耗时：{}", endTime - startTime);
		/*if (!searchHits.isEmpty()) {
			searchHits.forEach(hit -> {
				log.info("内容：{}", JSONUtil.toJsonStr(hit.getContent()));
			});
		}*/
	}

	/**
	 * 搜索
	 */
	@Test
	public void testQuery2() {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.should(QueryBuilders.rangeQuery("sortNo").gt("5"))
				.must(QueryBuilders.termQuery("result", "更新"));
		searchSourceBuilder.query(boolQueryBuilder);
		// 分页
		searchSourceBuilder.from(0).size(5);

		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("demo-use-log2");
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			org.elasticsearch.search.SearchHit[] hits = response.getHits().getHits();
			for(org.elasticsearch.search.SearchHit searchHit : hits) {
				log.info("内容：{}", searchHit.getSourceAsString());
			}
		} catch(IOException ex) {
			ex.getStackTrace();
		}
	}

	/**
	 * 滚动搜索提升搜索信息
	 */
	@Test
	public void scollPage() {
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_DEMO_USER);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// hit 返回值(bool 查询返回条数), 查询 size 影响查询效率
		searchSourceBuilder.size(5000);
		// 准确计数
		searchSourceBuilder.trackTotalHits(true);
		// 超时时间
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		// 绑定查询条件
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		searchSourceBuilder.query(boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		// 开启scroll查询,设置scroll过期时间为1min
		searchRequest.scroll("1m");

		try {
			Long startTime = System.currentTimeMillis();
			SearchResponse response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			org.elasticsearch.search.SearchHit[] hits = response.getHits().getHits();
			for(org.elasticsearch.search.SearchHit searchHit : hits) {
				String sourceAsString = searchHit.getSourceAsString();
			}

			// 获取ScrollId
			String scrollId = response.getScrollId();

			int count = 0;
			// 查询深度同样会影响性能
			while (null != hits && hits.length > 0) {
				SearchScrollRequest scrollRequest = new SearchScrollRequest();
				scrollRequest.scrollId(scrollId);
				scrollRequest.scroll("1m");
				response = highLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
				// 更新查询结果
				hits = response.getHits().getHits();
				for(org.elasticsearch.search.SearchHit searchHit : hits) {
					String sourceAsString = searchHit.getSourceAsString();
				}
				scrollId = response.getScrollId();
				if(count > 200) {
					break;
				}
				count++;
			}
			Long endTime = System.currentTimeMillis();
			log.info("整体耗时：{}", endTime - startTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 聚合查询，根据createTime字段聚合；可以理解为SQL中的Group By
	 */
	@Test
	public void testAggregation() {
		NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
		nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("aggregationResult").field("createTime"));
		SearchHits<UseLogTwoDO> search = esTemplate.search(nativeSearchQueryBuilder.build(), UseLogTwoDO.class);
		Aggregations aggregations = search.getAggregations();
		ParsedLongTerms aggregationResult = aggregations.get("aggregationResult");
		aggregationResult.getBuckets().forEach(bucket -> {
			log.info(bucket.getKey() + ":" + bucket.getDocCount());
		});
	}

	/**
	 * 测试 ElasticTemplate 删除 index
	 */
	@Test
	public void testDeleteIndex() {
		IndexOperations indexOperations = esTemplate.indexOps(UseLogTwoDO.class);
		if(indexOperations.exists()){
			// 删除索引
			indexOperations.delete();
		}
	}
}
