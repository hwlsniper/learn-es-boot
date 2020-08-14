package com.learn.es.config;

import com.plumelog.core.TraceId;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.config.TraceFeignInterceptor
 * @description TODO
 * @date 2020/8/6 15:03
 */
@Slf4j
@Component
public class TraceFeignInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		String traceId = TraceId.logTraceID.get();
		log.info("【TraceFeignInterceptor】"+traceId);
		requestTemplate.header("TraceId",traceId);
	}
}
