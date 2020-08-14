package com.learn.es.service.search;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.search.HouseBucketDTO
 * @description TODO
 * @date 2020/8/12 20:07
 */
@Data
@AllArgsConstructor
public class HouseBucketDTO {

	/**
	 * 聚合bucket的key
	 */
	private String key;

	/**
	 * 聚合结果值
	 */
	private long count;
}
