package com.learn.es.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.SubwayStation
 * @description TODO
 * @date 2020/8/12 16:02
 */
@Data
@Entity
@Table(name = "subway_station")
public class SubwayStation implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "subway_id")
	private Long subwayId;

	private String name;
}
