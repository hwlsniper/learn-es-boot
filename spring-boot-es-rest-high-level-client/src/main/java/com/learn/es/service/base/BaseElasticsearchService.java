package com.learn.es.service.base;

import cn.hutool.core.bean.BeanUtil;
import com.learn.es.config.ElasticsearchProperties;
import com.learn.es.exception.ElasticsearchException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.RestClients;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.base.BaseElasticsearchService
 * @description TODO
 * @date 2020/8/5 11:40
 */
@Slf4j
public abstract class BaseElasticsearchService {

	@Resource
	protected RestHighLevelClient restHighLevelClient;

	@Resource
	private ElasticsearchProperties properties;

	protected static final RequestOptions COMMON_OPTIONS;

	static {
		RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
		// 默认缓冲限制为100MB，此处修改为30MB。
		builder.setHttpAsyncResponseConsumerFactory(new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024));
		COMMON_OPTIONS = builder.build();
	}

	/**
	 * create elasticsearch index (asyc)
	 *
	 * @param index elasticsearch index
	 */
	protected void createIndexRequest(String index) {
		try {
			CreateIndexRequest request = new CreateIndexRequest(index);
			// 设置 shard 数量和 replica 数量
			request.settings(Settings.builder().put("index.number_of_shards", properties.getIndex().getNumberOfShards()).put("index.number_of_replicas", properties.getIndex().getNumberOfReplicas()));
			CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, COMMON_OPTIONS);
			log.info(" whether all of the nodes have acknowledged the request : {}", createIndexResponse.isAcknowledged());
			log.info(" Indicates whether the requisite number of shard copies were started for each shard in the index before timing out :{}", createIndexResponse.isShardsAcknowledged());
		} catch(IOException ex) {
			throw new ElasticsearchException("创建索引 {" + index + "} 失败");
		}
	}

	/**
	 * delete elasticsearch index
	 *
	 * @param index elasticsearch index name
	 */
	protected void deleteIndexRequest(String index) {
		DeleteIndexRequest request = buildDeleteIndexRequest(index);
		try {
			restHighLevelClient.indices().delete(request, COMMON_OPTIONS);
		} catch (IOException e) {
			throw new ElasticsearchException("删除索引 {" + index + "} 失败");
		}
	}

	/**
	 * 构建删除 index request
	 * @param index
	 * @return
	 */
	private DeleteIndexRequest buildDeleteIndexRequest(String index) {
		return new DeleteIndexRequest(index);
	}

	/**
	 * build IndexRequest
	 *
	 * @param index  elasticsearch index name
	 * @param id     request object id
	 * @param object request object
	 * @return {@link org.elasticsearch.action.index.IndexRequest}
	 */
	protected static IndexRequest buildIndexRequest(String index, String id, Object object) {
		return new IndexRequest(index).id(id).source(BeanUtil.beanToMap(object), XContentType.JSON);
	}

	/**
	 * exec updateRequest
	 *
	 * @param index  elasticsearch index name
	 * @param id     Document id
	 * @param object request object
	 */
	protected void updateRequest(String index, String id, Object object) {
		try {
			UpdateRequest request = new UpdateRequest(index, id).doc(BeanUtil.beanToMap(object), XContentType.JSON);
			restHighLevelClient.update(request, COMMON_OPTIONS);
		} catch(IOException ex) {
			throw new ElasticsearchException("更新索引 {" + index + "} 数据 {" + object + "} 失败");
		}
	}

	/**
	 * exec deleteRequest
	 *
	 * @param index elasticsearch index name
	 * @param id    Document id
	 * @author fxbin
	 */
	protected void deleteRequest(String index, String id) {
		try {
			DeleteRequest deleteRequest = new DeleteRequest(index, id);
			restHighLevelClient.delete(deleteRequest, COMMON_OPTIONS);
		} catch(IOException ex) {
			throw new ElasticsearchException("删除索引 {" + index + "} 数据id {" + id + "} 失败");
		}
	}

	/**
	 * search all
	 *
	 * @param index elasticsearch index name
	 * @return {@link SearchResponse}
	 * @author fxbin
	 */
	protected SearchResponse search(String index) {
		SearchResponse response = null;
		SearchRequest searchRequest = new SearchRequest(index);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchAllQuery());
		searchRequest.source(sourceBuilder);
		try {
			response = restHighLevelClient.search(searchRequest, COMMON_OPTIONS);
		} catch(IOException ex) {
			throw new ElasticsearchException("搜索索引 {" + index + "} 数据失败");
		}
		return response;
	}
}
