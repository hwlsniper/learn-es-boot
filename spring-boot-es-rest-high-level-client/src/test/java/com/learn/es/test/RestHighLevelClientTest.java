package com.learn.es.test;

import com.learn.es.SpringBootEsRestHighLevelClientApplicationTests;
import com.learn.es.contants.ElasticsearchConstant;
import com.learn.es.model.Person;
import com.learn.es.service.PersonService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.test.RestHighLevelClientTest
 * @description 自定义方法操作 ES
 * @date 2020/8/5 15:42
 */
public class RestHighLevelClientTest extends SpringBootEsRestHighLevelClientApplicationTests {

	@Autowired
	private PersonService personService;

	/**
	 * 测试删除索引
	 */
	@Test
	public void deleteIndexTest() {
		personService.deleteIndex(ElasticsearchConstant.INDEX_NAME);
	}

	/**
	 * 测试创建索引
	 */
	@Test
	public void createIndexTest() {
		personService.createIndex(ElasticsearchConstant.INDEX_NAME);
	}

	/**
	 * 测试新增
	 */
	@Test
	public void insertTest() {
		List<Person> list = new ArrayList<>();
		list.add(Person.builder().age(11).birthday(new Date()).country("CN").id(1L).name("哈哈").remark("test1").build());
		list.add(Person.builder().age(22).birthday(new Date()).country("US").id(2L).name("hiahia").remark("test2").build());
		list.add(Person.builder().age(33).birthday(new Date()).country("ID").id(3L).name("呵呵").remark("test3").build());

		personService.insert(ElasticsearchConstant.INDEX_NAME, list);
	}

	/**
	 * 测试更新
	 */
	@Test
	public void updateTest() {
		Person person = Person.builder().age(33).birthday(new Date()).country("ID_update").id(3L).name("呵呵update").remark("test3_update").build();
		List<Person> list = new ArrayList<>();
		list.add(person);
		personService.update(ElasticsearchConstant.INDEX_NAME, list);
	}

	/**
	 * 测试删除
	 */
	@Test
	public void deleteTest() {
		personService.delete(ElasticsearchConstant.INDEX_NAME, Person.builder().id(1L).build());
	}

	/**
	 * 测试查询
	 */
	@Test
	public void searchListTest() {
		List<Person> personList = personService.searchList(ElasticsearchConstant.INDEX_NAME);
		System.out.println(personList);
	}
}
