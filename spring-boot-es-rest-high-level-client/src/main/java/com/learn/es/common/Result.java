package com.learn.es.common;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.common.Result
 * @description TODO
 * @date 2020/8/5 10:28
 */
@Data
public class Result<T> implements Serializable {

	/**
	 * 错误码
	 */
	private int errcode;

	/**
	 * 错误信息
	 */
	private String errmsg;

	/**
	 * 响应数据
	 */
	private T data;

	public Result() {
	}

	private Result(ResultCode resultCode) {
		this(resultCode.code, resultCode.msg);
	}

	private Result(ResultCode resultCode, T data) {
		this(resultCode.code, resultCode.msg, data);
	}

	private Result(int errcode, String errmsg) {
		this(errcode, errmsg, null);
	}

	private Result(int errcode, String errmsg, T data) {
		this.errcode = errcode;
		this.errmsg = errmsg;
		this.data = data;
	}



	/**
	 * 返回成功
	 *
	 * @param <T> 泛型标记
	 * @return 响应信息 {@code Result}
	 */
	public static <T> Result<T> success() {
		return new Result<>(ResultCode.SUCCESS);
	}


	/**
	 * 返回成功-携带数据
	 *
	 * @param data 响应数据
	 * @param <T> 泛型标记
	 * @return 响应信息 {@code Result}
	 */
	public static <T> Result<T> success(@Nullable T data) {
		return new Result<>(ResultCode.SUCCESS, data);
	}
}
