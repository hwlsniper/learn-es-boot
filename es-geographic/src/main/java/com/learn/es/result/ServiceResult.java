package com.learn.es.result;

import lombok.Data;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.result.ServiceResult
 * @description 服务接口通用结构
 * @date 2020/8/12 17:11
 */
@Data
public class ServiceResult<T> {

	private boolean success;
	private String message;
	private T result;

	public ServiceResult(boolean success) {
		this.success = success;
	}

	public ServiceResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public ServiceResult(boolean success, String message, T result) {
		this.success = success;
		this.message = message;
		this.result = result;
	}

	public static <T> ServiceResult<T> success() {
		return new ServiceResult<>(true);
	}

	public static <T> ServiceResult<T> of(T result) {
		ServiceResult<T> serviceResult = new ServiceResult<>(true);
		serviceResult.setResult(result);
		return serviceResult;
	}

	public static <T> ServiceResult<T> notFound() {
		return new ServiceResult<>(false, Message.NOT_FOUND.getValue());
	}

	public enum Message {
		NOT_FOUND("Not Found Resource!"),
		NOT_LOGIN("User not login!");

		private String value;

		Message(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
