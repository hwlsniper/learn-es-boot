package com.learn.es.service;

import com.learn.es.entity.GoodsInfo;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.GoodsService
 * @description TODO
 * @date 2020/8/5 20:35
 */
public interface GoodsService {

	/**
	 * create Index
	 *
	 * @author fxbin
	 * @param index elasticsearch index name
	 */
	void createIndex(String index);

	/**
	 * delete Index
	 *
	 * @author fxbin
	 * @param index elasticsearch index name
	 */
	void deleteIndex(String index);

	/**
	 * insert document source
	 *
	 * @author fxbin
	 * @param index elasticsearch index name
	 * @param list data source
	 */
	void insert(String index, List<GoodsInfo> list);

	/**
	 * update document source
	 *
	 * @author fxbin
	 * @param index elasticsearch index name
	 * @param list data source
	 */
	void update(String index, List<GoodsInfo> list);

	/**
	 * delete document source
	 *
	 * @author fxbin
	 * @param info delete data source and allow null object
	 */
	void delete(String index, @Nullable GoodsInfo info);

	/**
	 * search all doc records
	 *
	 * @author fxbin
	 * @param index elasticsearch index name
	 * @return person list
	 */
	List<GoodsInfo> searchList(String index);
}
