package com.learn.es.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.base.AppErrorController
 * @description web 全局异常配置
 * @date 2020/8/12 14:20
 */
@Controller
public class AppErrorController implements ErrorController {

	private static final String ERROR_PATH = "/error";

	private ErrorAttributes errorAttributes;

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

	@Autowired
	public AppErrorController(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}

	/**
	 * Web页面错误处理
	 */
	@RequestMapping(value = ERROR_PATH, produces = "text/html")
	public String errorPageHandler(HttpServletRequest request, HttpServletResponse response) {
		int status = response.getStatus();
		switch (status) {
			case 403:
				return "403";
			case 404:
				return "404";
			case 500:
				return "500";
		}

		return "index";
	}

	/**
	 * 除Web页面外的错误处理，比如Json/XML等
	 */
	@RequestMapping(value = ERROR_PATH)
	@ResponseBody
	public ApiResponse errorApiHandler(HttpServletRequest request) {
		//RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		WebRequest webRequest = new ServletWebRequest(request);

		// this.errorAttributes.getErrorAttributes(requestAttributes, false);
		Map<String, Object> attr = this.errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
		int status = getStatus(request);

		return ApiResponse.ofMessage(status, String.valueOf(attr.getOrDefault("message", "error")));
	}

	private int getStatus(HttpServletRequest request) {
		Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (status != null) {
			return status;
		}

		return 500;
	}
}
