package com.learn.es.listener;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.learn.es.event.UpdateCanalEvent;
import com.learn.es.service.ElasticsearchService;
import com.learn.es.service.MappingService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 修改监听器
 */
@Log4j2
@Component
public class UpdateCanalListener extends AbstractCanalListener<UpdateCanalEvent> {

    @Resource
    private MappingService mappingService;

    @Resource
    private ElasticsearchService elasticsearchService;

    @Override
    protected void doSync(String database, String table, String index, RowData rowData) {
        List<Column> columns = rowData.getAfterColumnsList();
        String primaryKey = Optional.ofNullable(mappingService.getTablePrimaryKeyMap().get(database + "." + table)).orElse("id");
        Column idColumn = columns.stream().filter(column -> column.getIsKey() && primaryKey.equals(column.getName())).findFirst().orElse(null);
        if (idColumn == null || StringUtils.isBlank(idColumn.getValue())) {
            log.warn("update_column_find_null_warn update从column中找不到主键,database=" + database + ",table=" + table);
            return;
        }
        log.debug("update_column_id_info update主键id,database=" + database + ",table=" + table + ",id=" + idColumn.getValue());
        Map<String, Object> dataMap = parseColumnsToMap(columns);
        elasticsearchService.update(index, idColumn.getValue(), dataMap);
        log.debug("update_es_info 同步es插入操作成功！database=" + database + ",table=" + table + ",data=" + dataMap);
    }
}
