package com.learn.es.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.model.Person
 * @description TODO
 * @date 2020/8/5 11:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {

	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 名字
	 */
	private String name;

	/**
	 * 国家
	 */
	private String country;

	/**
	 * 年龄
	 */
	private Integer age;

	/**
	 * 生日
	 */
	private Date birthday;

	/**
	 * 介绍
	 */
	private String remark;
}
