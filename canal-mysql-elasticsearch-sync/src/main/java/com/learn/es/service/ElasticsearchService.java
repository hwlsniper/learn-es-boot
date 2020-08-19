package com.learn.es.service;

import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.ElasticsearchService
 * @description TODO
 * @date 2020/8/17 11:10
 */
public interface ElasticsearchService {

	/**
	 * 添加 index 文档
	 * @param index     索引
	 * @param id        文档ID
	 * @param dataMap   文档数据
	 */
	void insertById(String index, String id, Map<String, Object> dataMap);

	/**
	 * 批量添加 index 文档
	 * @param index
	 * @param idDataMap
	 */
	void batchInsertById(String index, Map<String, Map<String, Object>> idDataMap);

	/**
	 * 修改 index 文档
	 * @param index
	 * @param id
	 * @param dataMap
	 */
	void update(String index, String id, Map<String, Object> dataMap);

	/**
	 * 删除文档
	 * @param index
	 * @param id
	 */
	void deleteById(String index, String id);
}
