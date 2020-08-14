package com.learn.es.repository;

import com.learn.es.model.Person;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.repository.PersonRepository
 * @description TODO
 * @date 2020/8/3 17:53
 */
public interface PersonRepository extends ElasticsearchRepository<Person, Long> {

	/**
	 * 根据年龄区间获取数据
	 * 方法的命名一定要和实体类中的字段匹配
	 * @param min
	 * @param max
	 * @return
	 */
	List<Person> findByAgeBetween(Integer min, Integer max);

	List<Person> findByName(String name);
}
