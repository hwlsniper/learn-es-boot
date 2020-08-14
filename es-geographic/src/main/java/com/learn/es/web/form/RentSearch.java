package com.learn.es.web.form;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.form.RentSearch
 * @description 租房请求参数结构体
 * @date 2020/8/11 17:16
 */
@Data
public class RentSearch implements Serializable {
	private String cityEnName;
	private String regionEnName;
	private String priceBlock;
	private String areaBlock;
	private int room;
	private int direction;
	private String keywords;
	private int rentWay = -1;
	private String orderBy = "lastUpdateTime";
	private String orderDirection = "desc";
	private int start = 0;
	private int size = 5;

	public int getStart() {
		return start > 0 ? start : 0;
	}

	public int getSize() {
		if (this.size < 1) {
			return 5;
		} else if (this.size > 100) {
			return 100;
		} else {
			return this.size;
		}
	}

	public int getRentWay() {
		if (rentWay > -2 && rentWay < 2) {
			return rentWay;
		} else {
			return -1;
		}
	}
}
