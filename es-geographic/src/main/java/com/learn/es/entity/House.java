package com.learn.es.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.House
 * @description TODO
 * @date 2020/8/12 16:08
 */
@Data
@Entity
@Table(name = "house")
public class House implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(name = "admin_id")
	private Long adminId;

	private int price;

	private int area;

	private int room;

	private int parlour;

	private int bathroom;

	private int floor;

	@Column(name = "total_floor")
	private int totalFloor;

	@Column(name = "watch_times")
	private int watchTimes;

	@Column(name = "build_year")
	private int buildYear;

	private int status;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "last_update_time")
	private Date lastUpdateTime;

	@Column(name = "city_en_name")
	private String cityEnName;

	@Column(name = "region_en_name")
	private String regionEnName;

	private String street;

	private String district;

	private int direction;

	private String cover;

	@Column(name = "distance_to_subway")
	private int distanceToSubway;
}
