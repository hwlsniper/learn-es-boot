package com.plumelog.server.collect;

import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.server.monitor.PlumelogMonitorEvent;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：BaseLogCollect
 * description：BaseLogCollect 基类
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class BaseLogCollect {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(BaseLogCollect.class);
    public ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    public ElasticLowerClient elasticLowerClient;
    protected ApplicationEventPublisher applicationEventPublisher;

    public String getRunLogIndex(){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
    }
    public String getTraceLogIndex(){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
    }
    public void sendLog(String index, List<String> sendList) {
        try {
            if(sendList.size()>0) {
                elasticLowerClient.insertList(sendList, index, LogMessageConstant.ES_TYPE);
                logger.info("logList insert es success! count:{}", sendList.size());
            }
        } catch (Exception e) {
            logger.error("logList insert es failed!", e);
        }
    }

    public void sendTraceLogList(String index, List<String> sendTraceLogList) {
        try {
            if(sendTraceLogList.size()>0) {
                elasticLowerClient.insertList(sendTraceLogList, index, LogMessageConstant.ES_TYPE);
                logger.info("traceLogList insert es success! count:{}", sendTraceLogList.size());
            }
        } catch (Exception e) {
            logger.error("traceLogList insert es failed!", e);
        }
    }

    protected void publisherMonitorEvent(List<String> logs) {
        if (logs.size()>0){
            try {
                applicationEventPublisher.publishEvent(new PlumelogMonitorEvent(this, logs));
            }catch (Exception e){
                logger.error("publisherMonitorEvent error!", e);
            }
        }

    }
}
