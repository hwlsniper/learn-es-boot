package com.learn.es.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.SupportAddressDTO
 * @description TODO
 * @date 2020/8/12 11:32
 */
@Data
public class SupportAddressDTO implements Serializable {

	private Long id;
	@JsonProperty(value = "belong_to")
	private String belongTo;

	@JsonProperty(value = "en_name")
	private String enName;

	@JsonProperty(value = "cn_name")
	private String cnName;

	private String level;

	/**
	 * 经度
	 */
	private double baiduMapLongitude;

	/**
	 * 纬度
	 */
	private double baiduMapLatitude;
}
