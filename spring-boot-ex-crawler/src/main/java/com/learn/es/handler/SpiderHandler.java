package com.learn.es.handler;

import cn.hutool.core.date.format.FastDateFormat;
import com.google.common.collect.Maps;
import com.learn.es.constant.ElasticsearchConstant;
import com.learn.es.constant.SysConstant;
import com.learn.es.service.GoodsService;
import com.learn.es.service.impl.SpiderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.handler.SpiderHandler
 * @description 爬虫调度处理器
 * @date 2020/8/5 17:31
 */
@Slf4j
@Component
public class SpiderHandler {

	@Autowired
	private SpiderService spiderService;

	@Autowired
	private GoodsService goodsService;

	public void createIndex() {
		//goodsService.createIndex(ElasticsearchConstant.INDEX_NAME);
		this.spiderData();
	}

	public void spiderData() {
		log.info("爬虫开始....");
		Date startDate = new Date();
		// 使用现线程池提交任务
		ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5,
				new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());

		//引入countDownLatch进行线程同步，使主线程等待线程池的所有任务结束，便于计时
		CountDownLatch countDownLatch = new CountDownLatch(100);
		for(int i = 0; i < 300; i ++) {
			Map<String, String> params = Maps.newHashMap();
			params.put("keyword", "纸巾");
			params.put("enc", "utf-8");
			params.put("wc", "纸巾");
			params.put("page", i + "");
			executorService.submit(() -> {
				spiderService.spiderData(SysConstant.BASE_URL, params);
				countDownLatch.countDown();
			});
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
		Date endDate = new Date();

		FastDateFormat fdf = FastDateFormat.getInstance(SysConstant.DEFAULT_DATE_FORMAT);
		log.info("爬虫结束....");
		log.info("[开始时间:" + fdf.format(startDate) + ",结束时间:" + fdf.format(endDate) + ",耗时:"
				+ (endDate.getTime() - startDate.getTime()) + "ms]");

	}
}
