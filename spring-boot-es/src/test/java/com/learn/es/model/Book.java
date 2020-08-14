package com.learn.es.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.model.Book
 * @description TODO
 * @date 2020/8/7 17:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book implements Serializable {

	private Integer id;
	private Long seqNo;
	private LocalDateTime createTime;
	private Double price;
	private String title;
	private String remark;
}
