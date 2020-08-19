package com.learn.es.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.client.CanalClient
 * @description TODO
 * @date 2020/8/17 17:00
 */
@Log4j2
@Component
public class CanalClient implements DisposableBean {

	private CanalConnector canalConnector;

	@Value("${canal.host}")
	private String canalHost;
	@Value("${canal.port}")
	private String canalPort;
	@Value("${canal.destination}")
	private String canalDestination;
	@Value("${canal.username}")
	private String canalUsername;
	@Value("${canal.password}")
	private String canalPassword;

	@Bean
	public  CanalConnector getCanalConnector() {
		canalConnector = CanalConnectors.newClusterConnector(Lists.newArrayList(new InetSocketAddress(canalHost, Integer.valueOf(canalPort))),
				canalDestination, canalUsername, canalPassword);
		canalConnector.connect();
		// 指定filter，格式 {database}.{table}，这里不做过滤，过滤操作留给用户
		canalConnector.subscribe();
		// 回滚寻找上次中断的位置
		canalConnector.rollback();
		log.info("canal 客户端启动成功");
		return canalConnector;
	}

	@Override
	public void destroy() throws Exception {
		if(null != canalConnector) {
			canalConnector.disconnect();
		}
	}
}
