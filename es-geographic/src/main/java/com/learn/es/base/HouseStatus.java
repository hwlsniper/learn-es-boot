package com.learn.es.base;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.base.HouseStatus
 * @description 房源状态
 * @date 2020/8/12 14:05
 */
public enum HouseStatus {

	NOT_AUDITED(0), // 未审核
	PASSES(1), // 审核通过
	RENTED(2), // 已出租
	DELETED(3); // 逻辑删除
	private int value;

	HouseStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
