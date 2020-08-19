package com.learn.es.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.model.response.Response
 * @description 响应
 * @date 2020/8/17 10:52
 */
@Data
public class Response<T> implements Serializable {
	private int code = 0;
	private String message = "SUCCESS";
	private T data;

	public Response(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public Response(T data) {
		this.data = data;
	}

	public static <T> Response<T> success(T result) {
		return new Response<>(result);
	}
}
