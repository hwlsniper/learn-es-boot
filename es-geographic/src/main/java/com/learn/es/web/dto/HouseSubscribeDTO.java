package com.learn.es.web.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.HouseSubscribeDTO
 * @description 预约看房实体类
 * @date 2020/8/12 11:36
 */
@Data
public class HouseSubscribeDTO implements Serializable {

	private Long id;

	private Long houseId;

	private Long userId;

	private Long adminId;

	// 预约状态 1-加入待看清单 2-已预约看房时间 3-看房完成
	private int status;

	private Date createTime;

	private Date lastUpdateTime;

	private Date orderTime;

	private String telephone;

	private String desc;
}
