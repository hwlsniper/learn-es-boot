package com.learn.es.exception;

import com.learn.es.constant.ResultCode;
import lombok.Getter;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.exception.ElasticsearchException
 * @description TODO
 * @date 2020/8/5 11:16
 */
public class ElasticsearchException extends RuntimeException {

	@Getter
	private int errcode;

	@Getter
	private String errmsg;

	public ElasticsearchException(ResultCode resultCode) {
		this(resultCode.getCode(), resultCode.getMsg());
	}

	public ElasticsearchException(String message) {
		super(message);
	}

	public ElasticsearchException(Integer errcode, String errmsg) {
		super(errmsg);
		this.errcode = errcode;
		this.errmsg = errmsg;
	}

	public ElasticsearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public ElasticsearchException(Throwable cause) {
		super(cause);
	}

	public ElasticsearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
