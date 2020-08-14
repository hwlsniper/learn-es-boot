package com.learn.es.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.model.UseLogTwoDO
 * @description TODO
 * @date 2020/8/4 14:16
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "demo-use-log2")
public class UseLogTwoDO implements Serializable {

	@Id
	private String id;

	private Integer sortNo;

	@Field(type = FieldType.Keyword)
	private String result;

	private Date createTime;
}
