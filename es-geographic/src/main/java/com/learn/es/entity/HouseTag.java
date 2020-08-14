package com.learn.es.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.HouseTag
 * @description TODO
 * @date 2020/8/12 16:05
 */
@Data
@Entity
@Table(name = "house_tag")
public class HouseTag implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "house_id")
	private Long houseId;

	private String name;

	public HouseTag() {
	}

	public HouseTag(Long houseId, String name) {
		this.houseId = houseId;
		this.name = name;
	}
}
