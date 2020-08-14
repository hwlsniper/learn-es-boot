package com.learn.es.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.Role
 * @description TODO
 * @date 2020/8/12 16:04
 */
@Data
@Entity
@Table(name = "role")
public class Role implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	private String name;
}
