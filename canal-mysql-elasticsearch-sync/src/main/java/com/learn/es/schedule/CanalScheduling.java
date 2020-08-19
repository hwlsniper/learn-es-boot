package com.learn.es.schedule;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.learn.es.event.DeleteCanalEvent;
import com.learn.es.event.InsertCanalEvent;
import com.learn.es.event.UpdateCanalEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.schedule.CanalScheduling
 * @description 调度线程
 * @date 2020/8/17 15:33
 */
@Log4j2
@Component
public class CanalScheduling implements Runnable, ApplicationContextAware {

	private ApplicationContext applicationContext;

	/**
	 * canal 连接器
	 */
	@Resource
	private CanalConnector canalConnector;

	/**
	 * 调度间隔为 2s
	 */
	@Override
	@Scheduled(fixedDelay = 30000)
	public void run() {
		try {
			int batchSize = 2;
			// 通过 CanalConnector 获取 binlog 消息数据(或者 MQ 数据)
			Message message = canalConnector.getWithoutAck(batchSize);
			long batchId = message.getId();
			log.info("scheduled_batchId=" + batchId);
			try {
				List<CanalEntry.Entry> entries = message.getEntries();
				if(batchId != -1 && !entries.isEmpty()) {
					entries.forEach(entry -> {
						publishCanalEvent(entry);
					});
				}
				canalConnector.ack(batchId);
			} catch(Exception ex) {
				log.error("发送监听事件失败！batchId回滚,batchId=" + batchId, ex);
				canalConnector.rollback(batchId);
			}
		} catch(Exception ex) {
			log.error("方法:{};异常信息:{}", CanalScheduling.class.getName()+".run()", ex.getMessage());
		}
	}

	/**
	 * 根据事件类型发布相应的事件
	 * @param entry
	 */
	private void publishCanalEvent(CanalEntry.Entry entry) {
		CanalEntry.EventType eventType = entry.getHeader().getEventType();
		switch (eventType) {
			case INSERT:
				applicationContext.publishEvent(new InsertCanalEvent(entry));
				break;
			case UPDATE:
				applicationContext.publishEvent(new UpdateCanalEvent(entry));
				break;
			case DELETE:
				applicationContext.publishEvent(new DeleteCanalEvent(entry));
				break;
			default:
				log.warn("没有该事件类型");
				break;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
