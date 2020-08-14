package com.learn.es.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.HousePicture
 * @description TODO
 * @date 2020/8/12 16:19
 */
@Data
@Entity
@Table(name = "house_picture")
public class HousePicture implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "house_id")
	private Long houseId;

	private String path;

	@Column(name = "cdn_prefix")
	private String cdnPrefix;

	private int width;

	private int height;

	private String location;
}
