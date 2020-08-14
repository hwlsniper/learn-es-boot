package com.learn.es.service.search;

import lombok.Data;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.search.HouseSuggest
 * @description TODO
 * @date 2020/8/12 20:16
 */
@Data
public class HouseSuggest {

	private String input;
	// 默认权重
	private int weight = 10;
}
