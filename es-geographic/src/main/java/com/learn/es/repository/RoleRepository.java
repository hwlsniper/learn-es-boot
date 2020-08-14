package com.learn.es.repository;

import java.util.List;

import com.learn.es.entity.Role;
import org.springframework.data.repository.CrudRepository;

/**
 * 角色数据 DAO
 */
public interface RoleRepository extends CrudRepository<Role, Long> {
    List<Role> findRolesByUserId(Long userId);
}
