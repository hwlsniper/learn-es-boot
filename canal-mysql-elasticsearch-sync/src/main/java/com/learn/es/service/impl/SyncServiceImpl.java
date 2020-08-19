package com.learn.es.service.impl;

import com.learn.es.dao.BaseDao;
import com.learn.es.model.DatabaseTableModel;
import com.learn.es.model.IndexTypeModel;
import com.learn.es.model.request.SyncByTableRequest;
import com.learn.es.service.ElasticsearchService;
import com.learn.es.service.MappingService;
import com.learn.es.service.SyncService;
import com.learn.es.service.TransactionalService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.SyncServiceImpl
 * @description TODO
 * @date 2020/8/17 13:58
 */
@Log4j2
@Service
public class SyncServiceImpl implements SyncService, InitializingBean, DisposableBean {

	private ExecutorService cachedThreadPool;

	@Autowired
	private BaseDao baseDao;

	@Autowired
	private TransactionalService transactionalService;

	@Autowired
	private MappingService mappingService;

	@Override
	public boolean syncByTable(SyncByTableRequest request) {
		// 通过数据库和数据库表获取 index
		IndexTypeModel indexTypeModel = mappingService.getIndexType(new DatabaseTableModel(request.getDatabase(), request.getTable()));
		/**
		 * 如果value为null，那么就会手动创建一个new Optional()；但是this.value = null。所以这个时候就跳出了手动抛空指针那个代码块。这个时候，我们就可以使用orElse(T other)。
		 * 看源码，如果value == null，那么将取other的值。
		 */
		String primaryKey = Optional.ofNullable(mappingService.getTablePrimaryKeyMap().get(request.getDatabase() + "." + request.getTable())).orElse("id");
		if (indexTypeModel == null) {
			throw new IllegalArgumentException(String.format("配置文件中缺失database=%s和table=%s所对应的 index 的映射配置", request.getDatabase(), request.getTable()));
		}

		long minPK = Optional.ofNullable(request.getFrom()).orElse(baseDao.selectMinPK(primaryKey, request.getDatabase(), request.getTable()));
		long maxPK = Optional.ofNullable(request.getTo()).orElse(baseDao.selectMaxPK(primaryKey, request.getDatabase(), request.getTable()));

		cachedThreadPool.submit(() -> {
			try {
				for(long i = minPK; i <= maxPK + request.getStepSize(); i += request.getStepSize()) {
					transactionalService.batchInsertElasticsearch(request, primaryKey, i, i + request.getStepSize(), indexTypeModel);
					log.info(String.format("当前同步pk=%s，总共total=%s，进度=%s%%", i, maxPK, new BigDecimal(i * 100).divide(new BigDecimal(maxPK), 3, BigDecimal.ROUND_HALF_UP)));
				}
			} catch(Exception ex) {
				log.error("批量转换并插入Elasticsearch异常", ex);
			}
		});
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cachedThreadPool = new ThreadPoolExecutor(5, 10,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingDeque<>(10000), (ThreadFactory) Thread::new);
	}

	@Override
	public void destroy() throws Exception {
		if(null != cachedThreadPool) {
			cachedThreadPool.shutdown();
		}
	}
}
