package com.learn.es.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.Subway
 * @description TODO
 * @date 2020/8/12 16:03
 */
@Data
@Entity
@Table(name = "subway")
public class Subway implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(name = "city_en_name")
	private String cityEnName;
}
