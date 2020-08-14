package com.learn.es.service.search;

import com.learn.es.result.ServiceMultiResult;
import com.learn.es.result.ServiceResult;
import com.learn.es.web.form.MapSearch;
import com.learn.es.web.form.RentSearch;

import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.search.ISearchService
 * @description 检索接口
 * @date 2020/8/12 20:21
 */
public interface ISearchService {

	/**
	 * 索引目标房源
	 * @param houseId
	 */
	void index(Long houseId);

	/**
	 * 移除房源索引
	 * @param houseId
	 */
	void remove(Long houseId);

	/**
	 * 搜索房源
	 * @param rentSearch
	 * @return
	 */
	ServiceMultiResult<Long> query(RentSearch rentSearch);

	/**
	 * 获取补全建议关键词
	 */
	ServiceResult<List<String>> suggest(String prefix);

	/**
	 * 聚合特定小区的房间数
	 */
	ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);

	/**
	 * 聚合城市数据
	 * @param cityEnName
	 * @return
	 */
	ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName);

	/**
	 * 城市级别查询
	 * @return
	 */
	ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy,
	                                  String orderDirection, int start, int size);
	/**
	 * 精确范围数据查询
	 * @param mapSearch
	 * @return
	 */
	ServiceMultiResult<Long> mapQuery(MapSearch mapSearch);
}
