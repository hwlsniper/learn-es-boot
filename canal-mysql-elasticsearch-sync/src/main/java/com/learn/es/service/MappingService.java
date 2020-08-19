package com.learn.es.service;

import com.learn.es.model.DatabaseTableModel;
import com.learn.es.model.IndexTypeModel;

import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.MappingService
 * @description 配置文件的映射服务
 * @date 2020/8/17 11:14
 */
public interface MappingService {

	/**
	 * 通过 database和 table，获取与之对应的 index
	 *
	 * @param databaseTableModel mysql
	 * @return Elasticsearch
	 */
	IndexTypeModel getIndexType(DatabaseTableModel databaseTableModel);

	/**
	 * 通过 index，获取与之对应的 database和 table
	 *
	 * @param indexTypeModel Elasticsearch
	 * @return mysql
	 */
	DatabaseTableModel getDatabaseTableModel(IndexTypeModel indexTypeModel);

	/**
	 * 获取数据库表的主键映射
	 */
	Map<String, String> getTablePrimaryKeyMap();

	/**
	 * 设置数据库表的主键映射
	 */
	void setTablePrimaryKeyMap(Map<String, String> tablePrimaryKeyMap);

	/**
	 * 获取Elasticsearch的数据转换后类型
	 *
	 * @param mysqlType mysql数据类型
	 * @param data      具体数据
	 * @return Elasticsearch对应的数据类型
	 */
	Object getElasticsearchTypeObject(String mysqlType, String data);
}
