package com.learn.es.base;

import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.base.HouseSort
 * @description 排序生成器
 * @date 2020/8/12 14:08
 */
public class HouseSort {
	public static final String DEFAULT_SORT_KEY = "lastUpdateTime";

	public static final String DISTANCE_TO_SUBWAY_KEY = "distanceToSubway";

	private static final Set<String> SORT_KEYS = Sets.newHashSet(
			DEFAULT_SORT_KEY,
			"createTime",
			"price",
			"area",
			DISTANCE_TO_SUBWAY_KEY
	);

	public static Sort generateSort(String key, String directionKey) {
		key = getSortKey(key);

		Sort.Direction direction = Sort.Direction.fromString(directionKey);
		if (direction == null) {
			direction = Sort.Direction.DESC;
		}

		return Sort.by(direction, key);
	}

	public static String getSortKey(String key) {
		if (!SORT_KEYS.contains(key)) {
			key = DEFAULT_SORT_KEY;
		}

		return key;
	}
}
