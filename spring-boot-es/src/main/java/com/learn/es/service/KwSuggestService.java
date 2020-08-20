package com.learn.es.service;

import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.KwSuggestService
 * @description TODO
 * @date 2020/8/19 18:12
 */
public interface KwSuggestService {

	/**
	 * 搜索提示
	 * @param kw  搜索前缀
	 * @return
	 */
	List<String> GetKwSuggestList(String kw);
}
