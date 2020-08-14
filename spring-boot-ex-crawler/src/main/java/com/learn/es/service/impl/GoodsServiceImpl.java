package com.learn.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.learn.es.entity.GoodsInfo;
import com.learn.es.service.GoodsService;
import com.learn.es.service.base.BaseElasticsearchService;
import lombok.extern.slf4j.Slf4j;
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
 * @className com.learn.es.service.impl.GoodsServiceImpl
 * @description TODO
 * @date 2020/8/5 20:38
 */
@Slf4j
@Service
public class GoodsServiceImpl extends BaseElasticsearchService implements GoodsService {

	@Override
	public void createIndex(String index) {
		createIndexRequest(index);
	}

	@Override
	public void deleteIndex(String index) {
		deleteIndexRequest(index);
	}

	@Override
	public void insert(String index, List<GoodsInfo> list) {
		try {
			list.stream().forEach(goodsInfo -> {
				IndexRequest indexRequest = buildIndexRequest(index, String.valueOf(goodsInfo.getGoodsId()), goodsInfo);
				try {
					restHighLevelClient.index(indexRequest, COMMON_OPTIONS);
				} catch (Exception e) {
					log.info("保存索引失败:{}", e.getMessage());
					e.printStackTrace();
				}
			});
		} finally {
			/*try {
				restHighLevelClient.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}*/
		}
	}

	@Override
	public void update(String index, List<GoodsInfo> list) {
		list.stream().forEach(goodsInfo -> {
			updateRequest(index, String.valueOf(goodsInfo.getGoodsId()), goodsInfo);
		});
	}

	@Override
	public void delete(String index, GoodsInfo goodsInfo) {
		if(BeanUtil.isEmpty(goodsInfo)) {
			return;
		}
		deleteRequest(index, String.valueOf(goodsInfo.getGoodsId()));
	}

	@Override
	public List<GoodsInfo> searchList(String index) {
		SearchResponse response = search(index);
		SearchHit[] hits = response.getHits().getHits();
		List<GoodsInfo> list = new ArrayList<>();
		Arrays.stream(hits).forEach(hit -> {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			GoodsInfo goodsInfo = BeanUtil.mapToBean(sourceAsMap, GoodsInfo.class, true);
			list.add(goodsInfo);
		});
		return list;
	}
}
