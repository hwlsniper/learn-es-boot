package com.learn.es.web.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.form.DatatableSearch
 * @description TODO
 * @date 2020/8/11 17:14
 */
@Data
public class DatatableSearch implements Serializable {

	/**
	 * Datatables要求回显字段
	 */
	private int draw;

	/**
	 * Datatables规定分页字段
	 */
	private int start;
	private int length;

	private Integer status;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createTimeMin;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createTimeMax;

	private String city;
	private String title;
	private String direction;
	private String orderBy;
}
