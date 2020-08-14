package com.learn.es.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.entity.User
 * @description TODO
 * @date 2020/8/12 15:50
 */
@Data
@Entity
@Table(name = "user")
public class User implements UserDetails, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String password;

	private String email;

	@Column(name = "phone_number")
	private String phoneNumber;

	private int status;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "last_login_time")
	private Date lastLoginTime;

	@Column(name = "last_update_time")
	private Date lastUpdateTime;

	private String avatar;

	@Transient
	private List<GrantedAuthority> authorityList;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorityList;
	}

	@Override
	public String getUsername() {
		return name;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
