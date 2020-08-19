package com.learn.es.service.impl;

import com.learn.es.service.ElasticsearchService;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.ElasticsearchServiceImpl
 * @description TODO
 * @date 2020/8/17 11:35
 */
@Log4j2
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

	@Resource
	private RestHighLevelClient restHighLevelClient;

	@Override
	public void insertById(String index, String id, Map<String, Object> dataMap) {
		IndexRequest indexRequest = new IndexRequest(index).id(id).source(dataMap);
		try {
			restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", ElasticsearchServiceImpl.class.getName()+".insertById()", e.getMessage());
		}
	}

	@Override
	public void batchInsertById(String index, Map<String, Map<String, Object>> idDataMap) {
		BulkRequest bulkRequest = new BulkRequest();
		idDataMap.forEach((k, v) -> {
			bulkRequest.add(new IndexRequest(index).id(k).source(v));
		});
		try {
			restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", ElasticsearchServiceImpl.class.getName()+".batchInsertById()", e.getMessage());
		}
	}

	@Override
	public void update(String index, String id, Map<String, Object> dataMap) {
		this.insertById(index, id, dataMap);
	}

	@Override
	public void deleteById(String index, String id) {
		DeleteRequest deleteRequest = new DeleteRequest(index, id);
		try {
			restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", ElasticsearchServiceImpl.class.getName()+".deleteById()", e.getMessage());
		}
	}
}
