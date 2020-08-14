package com.learn.es.base;

import lombok.Data;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.base.ApiDataTableResponse
 * @description TODO
 * @date 2020/8/12 14:31
 */
@Data
public class ApiDataTableResponse extends ApiResponse {

	private int draw;
	private long recordsTotal;
	private long recordsFiltered;

	public ApiDataTableResponse(ApiResponse.Status status) {
		this(status.getCode(), status.getStandardMessage(), null);
	}

	public ApiDataTableResponse(int code, String message, Object data) {
		super(code, message, data);
	}
}
