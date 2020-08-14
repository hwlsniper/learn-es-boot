package com.learn.es.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.GoodsInfo
 * @description 商品
 * @date 2020/8/5 16:57
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName="goods_info", shards = 1, replicas = 0)
public class GoodsInfo implements Serializable {

	@Field(type = FieldType.Keyword)
	private String goodsId;
	@Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
	private String goodsName;
	@Field(type = FieldType.Keyword)
	private String imgUrl;
	@Field(type = FieldType.Double)
	private Double goodsPrice;
	@Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
	private String shopName;
}
