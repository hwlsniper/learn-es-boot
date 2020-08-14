package com.learn.es.template;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.template.ESBaseOptionTest
 * @description ES 基础操作
 * @date 2020/8/7 9:01
 */
@Slf4j
public class ESBaseOptionTest {

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

	/**
	 * 插入一条数据（如果存在则更新）
	 */
	public static void index() {
		RestHighLevelClient client = getClient();

		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("user", "kimchy");
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");

		IndexRequest indexRequest = new IndexRequest();
		indexRequest.index("user_temp").id("1").source(jsonMap);

		IndexResponse indexResponse = null;

		try {
			indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			close(client);
		}

		log.info("索引:{}", indexResponse.getIndex());
	}

	/**
	 * 获取一个文档
	 */
	public static void getDoc() {
		RestHighLevelClient client = getClient();

		GetRequest getRequest = new GetRequest("user_temp", "1");
		try {
			GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
			if(getResponse.isExists()) {
				String id = getResponse.getId();
				String index = getResponse.getIndex();
				long version = getResponse.getVersion();
				String sourceAsString = getResponse.getSourceAsString();
				Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
				byte[] sourceAsBytes = getResponse.getSourceAsBytes();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 文档是否存在
	 */
	public static void exists() {
		RestHighLevelClient client = getClient();
		GetRequest getRequest = new GetRequest("user_temp", "1");
		// 禁止获取_source
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		// 禁止获取存储字段
		getRequest.storedFields("_none_");

		try {
			boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
			log.info("文档是否存在:{}", exists);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 删除文档
	 */
	public static void delete() {
		RestHighLevelClient client = getClient();
		DeleteRequest deleteRequest = new DeleteRequest("user_temp", "1");

		try {
			DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
			exists();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 更新
	 */
	public static void update() {
		RestHighLevelClient client = getClient();
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("updated", new Date());
		jsonMap.put("reason", "daily update");
		UpdateRequest request = new UpdateRequest("user", "2").doc(jsonMap);

		try {
			UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 批量更新
	 */
	public static void bulk() {
		RestHighLevelClient client = getClient();

		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.add(new IndexRequest().index("user_temp").id("3").source(XContentType.JSON, "field", "foo", "user", "lucky"));
		bulkRequest.add(new IndexRequest("user").id("4").source(XContentType.JSON, "field", "bar", "user", "Jon"));
		bulkRequest.add(new IndexRequest("user").id("5").source(XContentType.JSON, "field", "baz", "user", "Lucy"));
		bulkRequest.add(new DeleteRequest("user", "3"));
		bulkRequest.add(new UpdateRequest("user", "2").doc(XContentType.JSON, "other", "test"));

		try {
			BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			RestStatus status = response.status();
			log.info("批量更新状态:{}", status);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(client);
		}
	}

	/**
	 * 批量查询
	 */
	public static void multiGet() {
		RestHighLevelClient client = getClient();

		MultiGetRequest multiGetRequest = new MultiGetRequest();
		multiGetRequest.add(new MultiGetRequest.Item("user_temp", "3"));
		multiGetRequest.add(new MultiGetRequest.Item("user_temp", "4"));
		multiGetRequest.add(new MultiGetRequest.Item("user_temp", "5"));

		try {
			MultiGetResponse response = client.mget(multiGetRequest, RequestOptions.DEFAULT);
			response.forEach(item -> System.out.println(item.getResponse().getSourceAsString()));
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
}
