package com.learn.es.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.google.protobuf.InvalidProtocolBufferException;
import com.learn.es.event.AbstractCanalEvent;
import com.learn.es.model.DatabaseTableModel;
import com.learn.es.model.IndexTypeModel;
import com.learn.es.service.MappingService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.listener.AbstractCanalListener
 * @description 事件监听抽象类
 * @date 2020/8/17 16:20
 */
@Log4j2
public abstract class AbstractCanalListener<EVENT extends AbstractCanalEvent> implements ApplicationListener<EVENT> {

	@Autowired
	private MappingService mappingService;

	@Override
	public void onApplicationEvent(EVENT event) {
		CanalEntry.Entry entry = event.getEntry();
		String database = entry.getHeader().getSchemaName();
		String table = entry.getHeader().getTableName();
		IndexTypeModel indexType = mappingService.getIndexType(new DatabaseTableModel(database, table));
		if(null == indexType) {
			return;
		}
		String index = indexType.getIndex();
		if(StringUtils.isBlank(index)) {
			return;
		}
		RowChange rowChange = null;
		try {
			rowChange = RowChange.parseFrom(entry.getStoreValue());
		} catch (InvalidProtocolBufferException e) {
			log.error("canalEntry_parser_error,根据CanalEntry获取RowChange失败！", e);
			return;
		}
		rowChange.getRowDatasList().forEach(rowData -> {
			doSync(database, table, index, rowData);
		});
	}

	Map<String, Object> parseColumnsToMap(List<CanalEntry.Column> columns) {
		Map<String, Object> jsonMap = new HashMap<>();
		columns.forEach(column -> {
			if (column == null) {
				return;
			}
			jsonMap.put(column.getName(), column.getIsNull() ? null : mappingService.getElasticsearchTypeObject(column.getMysqlType(), column.getValue()));
		});
		return jsonMap;
	}

	protected abstract void doSync(String database, String table, String index, CanalEntry.RowData rowData);
}
