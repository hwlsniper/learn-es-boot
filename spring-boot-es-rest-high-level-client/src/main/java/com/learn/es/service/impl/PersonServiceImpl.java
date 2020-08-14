package com.learn.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.learn.es.model.Person;
import com.learn.es.service.PersonService;
import com.learn.es.service.base.BaseElasticsearchService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.PersonServiceImpl
 * @description TODO
 * @date 2020/8/5 15:13
 */
@Service
public class PersonServiceImpl extends BaseElasticsearchService implements PersonService {

	@Override
	public void createIndex(String index) {
		createIndexRequest(index);
	}

	@Override
	public void deleteIndex(String index) {
		deleteIndexRequest(index);
	}

	@Override
	public void insert(String index, List<Person> list) {
		try {
			list.stream().forEach(person -> {
				IndexRequest indexRequest = buildIndexRequest(index, String.valueOf(person.getId()), person);
				try {
					restHighLevelClient.index(indexRequest, COMMON_OPTIONS);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} finally {
			try {
				restHighLevelClient.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void update(String index, List<Person> list) {
		list.stream().forEach(person -> {
			updateRequest(index, String.valueOf(person.getId()), person);
		});
	}

	@Override
	public void delete(String index, Person person) {
		if(BeanUtil.isEmpty(person)) {
			return;
		}
		deleteRequest(index, String.valueOf(person.getId()));
	}

	@Override
	public List<Person> searchList(String index) {
		SearchResponse response = search(index);
		SearchHit[] hits = response.getHits().getHits();
		List<Person> list = new ArrayList<>();
		Arrays.stream(hits).forEach(hit -> {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			Person person = BeanUtil.mapToBean(sourceAsMap, Person.class, true);
			list.add(person);
		});
		return list;
	}
}
