package com.learn.es.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.HousePictureDTO
 * @description 房屋图片大小
 * @date 2020/8/12 11:37
 */
@Data
public class HousePictureDTO implements Serializable {

	private Long id;

	@JsonProperty(value = "house_id")
	private Long houseId;

	private String path;

	@JsonProperty(value = "cdn_prefix")
	private String cdnPrefix;

	private int width;

	private int height;
}
