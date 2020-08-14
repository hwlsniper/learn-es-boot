package com.learn.es.security;

import com.learn.es.entity.User;
import com.learn.es.service.IUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.security.AuthProvider
 * @description 自定义认证实现
 * @date 2020/8/12 16:25
 */
@Log4j2
public class AuthProvider implements AuthenticationProvider {

	@Autowired
	private IUserService userService;

	//private final Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String userName = authentication.getName();
		String inputPassword = (String) authentication.getCredentials();

		User user = userService.findUserByName(userName);
		if (user == null) {
			log.error("方法:{};错误信息:{}", AuthProvider.class.getName()+".authenticate()", "authError");
			throw new AuthenticationCredentialsNotFoundException("authError");
		}

		// this.passwordEncoder.isPasswordValid(user.getPassword(), inputPassword, user.getId())
		if (passwordEncoder.matches(inputPassword, user.getPassword())) {
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

		}
		log.error("方法:{};错误信息:{}", AuthProvider.class.getName()+".authenticate()", "authError");
		throw new BadCredentialsException("authError");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}
}
