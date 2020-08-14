package com.learn.es.web.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.SubwayDTO
 * @description TODO
 * @date 2020/8/12 11:34
 */
@Data
public class SubwayDTO implements Serializable {

	private Long id;
	private String name;
	private String cityEnName;
}
