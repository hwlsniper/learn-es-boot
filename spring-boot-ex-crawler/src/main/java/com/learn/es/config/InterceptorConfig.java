package com.learn.es.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.config.InterceptorConfig
 * @description TODO
 * @date 2020/8/6 15:03
 */
@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

	@Autowired
	Interceptor interceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor);
		super.addInterceptors(registry);

	}
}
