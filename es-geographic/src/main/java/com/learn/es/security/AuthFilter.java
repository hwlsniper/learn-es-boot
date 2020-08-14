package com.learn.es.security;

import com.google.common.base.Strings;
import com.learn.es.base.LoginUserUtil;
import com.learn.es.entity.User;
import com.learn.es.service.ISmsService;
import com.learn.es.service.IUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.security.AuthFilter
 * @description 鉴权过滤器
 * @date 2020/8/12 16:30
 */
@Log4j2
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

	@Autowired
	private IUserService userService;

	@Autowired
	private ISmsService smsService;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String name = obtainUsername(request);
		if (!Strings.isNullOrEmpty(name)) {
			request.setAttribute("username", name);
			return super.attemptAuthentication(request, response);
		}

		String telephone = request.getParameter("telephone");
		if (Strings.isNullOrEmpty(telephone) || !LoginUserUtil.checkTelephone(telephone)) {
			log.error("方法:{};错误信息:{}", AuthFilter.class.getName()+".attemptAuthentication()", "Wrong telephone number");
			throw new BadCredentialsException("Wrong telephone number");
		}

		User user = userService.findUserByTelephone(telephone);
		String inputCode = request.getParameter("smsCode");
		String sessionCode = smsService.getSmsCode(telephone);
		if (Objects.equals(inputCode, sessionCode)) {
			if (user == null) {
				// 如果用户第一次用手机登录 则自动注册该用户
				user = userService.addUserByPhone(telephone);
			}
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		} else {
			log.error("方法:{};错误信息:{}", AuthFilter.class.getName()+".attemptAuthentication()", "smsCodeError");
			throw new BadCredentialsException("smsCodeError");
		}
	}
}
