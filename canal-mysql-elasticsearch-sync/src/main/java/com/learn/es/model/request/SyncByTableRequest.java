package com.learn.es.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.model.request.SyncByTableRequest
 * @description 同步表数据请求
 * @date 2020/8/17 10:51
 */
@Data
public class SyncByTableRequest implements Serializable {

	private String database;
	private String table;
	private Integer stepSize = 500;
	private Long from;
	private Long to;
}
