package com.learn.es.template;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.template.ParentChildSearchTest
 * @description parent-child关系
 * @date 2020/8/10 14:35
 */
@Slf4j
public class ParentChildSearchTest {

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

	// https://blog.csdn.net/u014646662/article/details/100081257

	/**
	 * 根据父文档id查询相关子文档
	 */
	private static void parentId() {
		RestHighLevelClient client = getClient();

		SearchRequest searchRequest = new SearchRequest("my_blogs");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		//子文档名
		String child_type = "comment";
		//父文档ID
		String id = "blog2";
		//ParentId查询
		ParentIdQueryBuilder parentIdQueryBuilder = new ParentIdQueryBuilder(child_type, id);
		searchSourceBuilder.query(parentIdQueryBuilder);
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(10);
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

			searchResponse.getHits().forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 通过ID和routing ，访问子文档(不加routing查不到)
	 */
	private static void childID() {
		RestHighLevelClient client = getClient();

		// comment3 子文档的 ID
		GetRequest getRequest = new GetRequest("my_blogs", "comment3");
		getRequest.routing("blog2");
		try {
			GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);

			if(response.isExists()) {
				String sourceAsString = response.getSourceAsString();
				log.info("数据:{}", sourceAsString);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}

	}

	/**
	 * 返回父文档
	 */
	private static void hasChild() {
		RestHighLevelClient client = getClient();

		SearchRequest  searchRequest = new SearchRequest ("my_blogs");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 子文档名
		String child_type = "comment";
		// 子文档查询条件
		QueryBuilder matchQuery = QueryBuilders.matchQuery("comment", "Elastic");
		// 是否计算评分
		ScoreMode scoreMode = ScoreMode.Total;
		HasChildQueryBuilder hasChildQueryBuilder = new HasChildQueryBuilder(child_type, matchQuery, scoreMode);
		searchSourceBuilder.query(hasChildQueryBuilder);
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(10);
		searchSourceBuilder.sort("_score");
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			searchResponse.getHits().forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}


	/**
	 * 返回相关的子文档
	 */
	private static void hasParent() {
		RestHighLevelClient client = getClient();
		SearchRequest request = new SearchRequest("my_blogs");
		SearchSourceBuilder builder = new SearchSourceBuilder();
		// 父文档名
		String parent_type = "blog";
		// 子文档查询条件
		QueryBuilder matchQuery = QueryBuilders.matchQuery("title", "hadoop learning");
		// 是否计算评分
		boolean score = true;
		HasParentQueryBuilder hasParentQueryBuilder = new HasParentQueryBuilder(parent_type, matchQuery, score);
		builder.query(hasParentQueryBuilder);
		builder.from(0);
		builder.size(10);
		// 按评分排序
		builder.sort("_score");
		request.source(builder);

		SearchResponse search = null;
		try {
			search = client.search(request, RequestOptions.DEFAULT);
			search.getHits().forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}

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

	public static void main(String[] args) {
		//parentId();

		//childID();

		hasChild();

		//hasParent();
	}
}
