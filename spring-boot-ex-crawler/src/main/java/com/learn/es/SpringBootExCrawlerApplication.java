package com.learn.es;

import com.learn.es.handler.SpiderHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class SpringBootExCrawlerApplication {

	@Autowired
	private SpiderHandler spiderHandler;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootExCrawlerApplication.class, args);
	}

	/*@PostConstruct
	public void task() {
		spiderHandler.createIndex();
	}*/
}
