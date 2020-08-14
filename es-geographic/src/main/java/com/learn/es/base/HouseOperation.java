package com.learn.es.base;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.base.HouseOperation
 * @description 房源操作状态
 * @date 2020/8/12 14:18
 */
public class HouseOperation {

	// 通过审核
	public static final int PASS = 1;

	// 下架, 重新审核
	public static final int PULL_OUT = 2;

	// 逻辑删除
	public static final int DELETE = 3;

	// 出租
	public static final int RENT = 4;
}
