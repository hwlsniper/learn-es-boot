package com.learn.es.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.security.LoginAuthFailHandler
 * @description 登录验证失败处理器
 * @date 2020/8/12 16:24
 */
public class LoginAuthFailHandler extends SimpleUrlAuthenticationFailureHandler {

	private final LoginUrlEntryPoint urlEntryPoint;

	public LoginAuthFailHandler(LoginUrlEntryPoint urlEntryPoint) {
		this.urlEntryPoint = urlEntryPoint;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
	                                    AuthenticationException exception) throws IOException, ServletException {
		String targetUrl = this.urlEntryPoint.determineUrlToUseForThisRequest(request, response, exception);

		targetUrl += "?" + exception.getMessage();
		super.setDefaultFailureUrl(targetUrl);
		super.onAuthenticationFailure(request, response, exception);
	}
}
