package com.learn.es.service.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.learn.es.model.DatabaseTableModel;
import com.learn.es.model.IndexTypeModel;
import com.learn.es.service.MappingService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.MappingServiceImpl
 * @description TODO
 * @date 2020/8/17 14:20
 */
@Log4j2
@Service
@PropertySource("classpath:mapping.properties")
@ConfigurationProperties(prefix = "index")
public class MappingServiceImpl implements MappingService, InitializingBean {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private Map<String, String> dbEsMapping;
	private BiMap<DatabaseTableModel, IndexTypeModel> dbEsBiMapping;
	private Map<String, String> tablePrimaryKeyMap;
	private Map<String, Converter> mysqlTypeElasticsearchTypeMapping;

	@Override
	public Map<String, String> getTablePrimaryKeyMap() {
		// 获取数据库表的主键映射
		return tablePrimaryKeyMap;
	}

	@Override
	public void setTablePrimaryKeyMap(Map<String, String> tablePrimaryKeyMap) {
		// 设置数据库表的主键映射
		this.tablePrimaryKeyMap = tablePrimaryKeyMap;
	}

	@Override
	public IndexTypeModel getIndexType(DatabaseTableModel databaseTableModel) {
		// 根据数据库和表获取 index
		return dbEsBiMapping.get(databaseTableModel);
	}

	@Override
	public DatabaseTableModel getDatabaseTableModel(IndexTypeModel indexTypeModel) {
		// 根据 index 获取数据库和表
		return dbEsBiMapping.inverse().get(indexTypeModel);
	}

	@Override
	public Object getElasticsearchTypeObject(String mysqlType, String data) {
		// 获取Elasticsearch的数据转换后类型
		Optional<Entry<String, Converter>> result = mysqlTypeElasticsearchTypeMapping.entrySet().parallelStream().filter(entry -> mysqlType.toLowerCase().contains(entry.getKey())).findFirst();
		return (result.isPresent() ? result.get().getValue() : (Converter) data1 -> data1).convert(data);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dbEsBiMapping = HashBiMap.create();
		dbEsMapping.forEach((key, value) -> {
			String[] keyStrings = StringUtils.split(key, ".");
			String[] valueStrings = StringUtils.split(value, ".");
			dbEsBiMapping.put(new DatabaseTableModel(keyStrings[0], keyStrings[1]), new IndexTypeModel(valueStrings[0]));
		});

		mysqlTypeElasticsearchTypeMapping = Maps.newHashMap();
		mysqlTypeElasticsearchTypeMapping.put("char", data -> data);
		mysqlTypeElasticsearchTypeMapping.put("text", data -> data);
		mysqlTypeElasticsearchTypeMapping.put("blob", data -> data);
		mysqlTypeElasticsearchTypeMapping.put("int", Long::valueOf);
		mysqlTypeElasticsearchTypeMapping.put("date", data -> LocalDateTime.parse(data, FORMATTER));
		mysqlTypeElasticsearchTypeMapping.put("time", data -> LocalDateTime.parse(data, FORMATTER));
		mysqlTypeElasticsearchTypeMapping.put("float", Double::valueOf);
		mysqlTypeElasticsearchTypeMapping.put("double", Double::valueOf);
		mysqlTypeElasticsearchTypeMapping.put("decimal", Double::valueOf);
	}

	public Map<String, String> getDbEsMapping() {
		return dbEsMapping;
	}

	public void setDbEsMapping(Map<String, String> dbEsMapping) {
		this.dbEsMapping = dbEsMapping;
	}

	private interface Converter {
		Object convert(String data);
	}
}
