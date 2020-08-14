package com.learn.es.config;

import com.plumelog.trace.aspect.AbstractAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.aspect.AspectConfig
 * @description TODO
 * @date 2020/8/6 14:48
 */
@Aspect
@Component
public class AspectConfig extends AbstractAspect {

	@Around("within(com.learn.es..*))")
	public Object around(JoinPoint joinPoint) throws Throwable {
		return aroundExecute(joinPoint);
	}
}
