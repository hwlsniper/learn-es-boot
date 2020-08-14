package com.learn.es.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.common.ResultCode
 * @description TODO
 * @date 2020/8/5 10:30
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

	/**
	 * 接口调用成功
	 */
	SUCCESS(0, "Request Successful"),

	/**
	 * 服务器暂不可用，建议稍候重试。建议重试次数不超过3次。
	 */
	FAILURE(-1, "System Busy");

	final int code;

	final String msg;
}
