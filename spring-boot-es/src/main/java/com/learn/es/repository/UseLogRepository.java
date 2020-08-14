package com.learn.es.repository;

import com.learn.es.model.UseLogDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.repository.UseLogRepository
 * @description TODO
 * @date 2020/8/4 20:46
 */
public interface UseLogRepository extends ElasticsearchRepository<UseLogDO, String> {

	/**
	 * 查找时间段内日志
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<UseLogDO> findByCreateTimeBetween(long startTime, long endTime);

	/**
	 * 分页查询
	 * @param start
	 * @param end
	 * @param page
	 * @return
	 */
	Page<UseLogDO> findBySortNoIsBetween(int start, int end, Pageable page);

	/**
	 * 删除大于deleteStartNo的日志
	 * @param deleteStartNo
	 * @return
	 */
	Long deleteBySortNoIsGreaterThan(int deleteStartNo);
}
