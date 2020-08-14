package com.learn.es.web.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.SubwayStationDTO
 * @description TODO
 * @date 2020/8/12 11:33
 */
@Data
public class SubwayStationDTO implements Serializable {

	private Long id;
	private Long subwayId;
	private String name;
}
