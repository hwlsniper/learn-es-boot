package com.learn.es.service;

import com.learn.es.model.IndexTypeModel;
import com.learn.es.model.request.SyncByTableRequest;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.TransactionalService
 * @description 同步数据到 ES
 * @date 2020/8/17 11:27
 */
public interface TransactionalService {

	/**
	 * 开启事务，读取mysql并插入到Elasticsearch中（读锁）
	 * @param request
	 * @param primaryKey        主键
	 * @param from              开始位置
	 * @param to                结束位置
	 * @param indexTypeModel    索引
	 */
	void batchInsertElasticsearch(SyncByTableRequest request, String primaryKey, long from, long to, IndexTypeModel indexTypeModel);
}
