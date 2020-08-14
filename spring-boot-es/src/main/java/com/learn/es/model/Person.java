package com.learn.es.model;

import com.learn.es.constants.ESConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.model.Person
 * @description TODO
 * @date 2020/8/3 17:48
 */
@Document(indexName = ESConstant.INDEX_PERSON, shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {
	/**
	 * 主键
	 */
	@Id
	private Long id;

	/**
	 * 名字
	 */
	@Field(type = FieldType.Keyword)
	private String name;

	/**
	 * 国家
	 */
	@Field(type = FieldType.Keyword)
	private String country;

	/**
	 * 年龄
	 */
	@Field(type = FieldType.Integer)
	private Integer age;

	/**
	 * 生日
	 */
	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	private LocalDate birthday;

	/**
	 * 介绍
	 */
	@Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
	private String remark;
}
