package com.learn.es.web.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.HouseDetailDTO
 * @description 房屋详情
 * @date 2020/8/12 11:38
 */
@Data
public class HouseDetailDTO implements Serializable {

	private String description;

	private String layoutDesc;

	private String traffic;

	private String roundService;

	private int rentWay;

	private Long adminId;

	private String address;

	private Long subwayLineId;

	private Long subwayStationId;

	private String subwayLineName;

	private String subwayStationName;
}
