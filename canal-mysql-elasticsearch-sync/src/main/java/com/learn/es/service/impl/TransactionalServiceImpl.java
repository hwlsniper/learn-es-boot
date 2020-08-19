package com.learn.es.service.impl;

import com.learn.es.dao.BaseDao;
import com.learn.es.model.IndexTypeModel;
import com.learn.es.model.request.SyncByTableRequest;
import com.learn.es.service.ElasticsearchService;
import com.learn.es.service.TransactionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.TransactionalServiceImpl
 * @description TODO
 * @date 2020/8/17 11:31
 */
@Service
public class TransactionalServiceImpl implements TransactionalService {

	@Resource
	private BaseDao baseDao;

	@Autowired
	private ElasticsearchService elasticsearchService;

	/**
	 * `@Transactional` 注解对 elasticsearch 也有用吗?
	 * @param request
	 * @param primaryKey
	 * @param from
	 * @param to
	 * @param indexTypeModel
	 */
	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public void batchInsertElasticsearch(SyncByTableRequest request, String primaryKey, long from, long to,
	                                     IndexTypeModel indexTypeModel) {
		// 获取数据
		List<Map<String, Object>> dataList = baseDao.selectByPKIntervalLockInShareMode(primaryKey, from, to, request.getDatabase(), request.getTable());
		if (dataList == null || dataList.isEmpty()) {
			return;
		}
		// 对数据时间戳进行转换
		dataList = convertDateType(dataList);
		// 数据结构进行转化 key-> pkId, value -> map
		Map<String, Map<String, Object>> dataMap = dataList.parallelStream().collect(Collectors.toMap(strObjMap -> String.valueOf(strObjMap.get(primaryKey)), map -> map));
		// 将数据批量添加到 ES 中
		elasticsearchService.batchInsertById(indexTypeModel.getIndex(), dataMap);
	}

	private List<Map<String, Object>> convertDateType(List<Map<String, Object>> source) {
		source.parallelStream().forEach(map -> map.forEach((key, value) -> {
			if(value instanceof Timestamp) {
				map.put(key, LocalDateTime.ofInstant(((Timestamp) value).toInstant(), ZoneId.systemDefault()));
			}
		}));
		return source;
	}
}
