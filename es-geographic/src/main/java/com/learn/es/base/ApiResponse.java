package com.learn.es.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.base.ApiResponse
 * @description API 格式封装
 * @date 2020/8/12 14:26
 */
public class ApiResponse {

	@Setter
	@Getter
	private int code;

	@Setter
	@Getter
	private String message;

	@Setter
	@Getter
	private Object data;

	@Setter
	@Getter
	private boolean more;

	public ApiResponse(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public ApiResponse() {
		this.code = Status.SUCCESS.getCode();
		this.message = Status.SUCCESS.getStandardMessage();
	}

	public static ApiResponse ofMessage(int code, String message) {
		return new ApiResponse(code, message, null);
	}

	public static ApiResponse ofSuccess(Object data) {
		return new ApiResponse(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
	}

	public static ApiResponse ofStatus(Status status) {
		return new ApiResponse(status.getCode(), status.getStandardMessage(), null);
	}

	public enum Status {
		SUCCESS(200, "OK"),
		BAD_REQUEST(400, "Bad Request"),
		NOT_FOUND(404, "Not Found"),
		INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
		NOT_VALID_PARAM(40005, "Not valid Params"),
		NOT_SUPPORTED_OPERATION(40006, "Operation not supported"),
		NOT_LOGIN(50000, "Not Login");

		private int code;
		private String standardMessage;

		Status(int code, String standardMessage) {
			this.code = code;
			this.standardMessage = standardMessage;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getStandardMessage() {
			return standardMessage;
		}

		public void setStandardMessage(String standardMessage) {
			this.standardMessage = standardMessage;
		}
	}
}
