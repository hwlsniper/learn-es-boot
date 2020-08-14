package com.learn.es.config;

import com.plumelog.core.TraceId;
import com.plumelog.core.util.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.config.Interceptor
 * @description TODO
 * @date 2020/8/6 14:57
 */
@Slf4j
@Component
public class Interceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String traceId = request.getHeader("TraceId");
		if (StringUtils.isEmpty(traceId)){
			IdWorker worker = new IdWorker(1,1,1);
			TraceId.logTraceID.set(String.valueOf(worker.nextId()));
		}else{
			TraceId.logTraceID.set(traceId);
		}
		log.info("[Interceptor]  "+TraceId.logTraceID.get());
		return true;
	}
}
