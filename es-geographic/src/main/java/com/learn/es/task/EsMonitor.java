package com.learn.es.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.task.ESMonitor
 * @description ES 监控
 * @date 2020/8/12 16:37
 */
@Log4j2
@Component
public class EsMonitor {
	private static final String HEALTH_CHECK_API = "http://127.0.0.1:9200/_cluster/health";

	private static final String GREEN = "green";
	private static final String YELLOW = "yellow";
	private static final String RED = "red";

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JavaMailSender mailSender;

	public void healthCheck() {
		HttpClient httpClient = HttpClients.createDefault();
		HttpGet get = new HttpGet(HEALTH_CHECK_API);

		try {
			HttpResponse response = httpClient.execute(get);
			if(response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
				log.error("Can not access ES Service normally! Please check the server.");
			} else {
				String body = EntityUtils.toString(response.getEntity(), "UTF-8");
				JsonNode result = objectMapper.readTree(body);
				String status = result.get("status").asText();

				String message = "";
				boolean isNormal = false;

				switch (status) {
					case GREEN:
						message = "ES server run normally.";
						isNormal = true;
						break;
					case YELLOW:
						message = "ES server gets status yellow! Please check the ES server!";
						break;
					case RED:
						message = "ES ser get status RED!!! Must Check ES Server!!!";
						break;
					default:
						message = "Unknown ES status from server for: " + status + ". Please check it.";
						break;
				}

				if (!isNormal) {
					// 发送邮件
					sendAlertMessage(message);
				}

				int numberNodes = result.get("number_of_data_nodes").asInt();
				if(numberNodes < 1) {
					log.error("Elasticsearch Node is missing");
				}
			}
		} catch(IOException ex) {
			log.error("方法:{},异常信息:{}", EsMonitor.class.getName()+".healthCheck()", ex.getMessage());
		}
	}

	/**
	 * 发送邮件
	 * @param message
	 */
	private void sendAlertMessage(String message) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("wenliang.he@163.com");
		mailMessage.setTo("1332601370@qq.com");
		mailMessage.setSubject("【警告】ES服务监控");
		mailMessage.setText(message);
		mailSender.send(mailMessage);
	}
}
