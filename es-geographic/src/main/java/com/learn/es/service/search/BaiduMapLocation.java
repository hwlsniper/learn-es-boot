package com.learn.es.service.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.search.BaiduMapLocation
 * @description 百度地理信息位置
 * @date 2020/8/12 20:05
 */
@Data
public class BaiduMapLocation {

	// 经度
	@JsonProperty("lon")
	private double longitude;

	// 纬度
	@JsonProperty("lat")
	private double latitude;
}
