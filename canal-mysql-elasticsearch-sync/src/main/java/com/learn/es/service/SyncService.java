package com.learn.es.service;

import com.learn.es.model.request.SyncByTableRequest;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.SyncService
 * @description TODO
 * @date 2020/8/17 11:26
 */
public interface SyncService {

	/**
	 * 通过 database 和 table 同步数据
	 *
	 * @param request 请求参数
	 * @return 后台同步进程执行成功与否
	 */
	boolean syncByTable(SyncByTableRequest request);
}
