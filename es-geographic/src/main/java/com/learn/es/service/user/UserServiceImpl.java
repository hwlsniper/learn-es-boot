package com.learn.es.service.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.learn.es.base.LoginUserUtil;
import com.learn.es.entity.Role;
import com.learn.es.entity.User;
import com.learn.es.repository.RoleRepository;
import com.learn.es.repository.UserRepository;
import com.learn.es.service.IUserService;
import com.learn.es.result.ServiceResult;
import com.learn.es.web.dto.UserDTO;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Log4j2
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    //private final Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findUserByName(String userName) {
        User user = userRepository.findByName(userName);

        if (user == null) {
            return null;
        }

        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            log.error("方法{};错误信息:{}", UserServiceImpl.class.getName()+"findUserByName()", "权限非法");
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        Optional<User> optional = userRepository.findById(userId);
        //User user = userRepository.findOne(userId);
        if (!optional.isPresent()) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(optional.get(), UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    @Override
    public User findUserByTelephone(String telephone) {
        User user = userRepository.findUserByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            log.error("方法{};错误信息:{}", UserServiceImpl.class.getName()+"findUserByTelephone()", "权限非法");
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    @Transactional
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0, 3) + "****" + telephone.substring(7, telephone.length()));
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setLastUpdateTime(now);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleRepository.save(role);
        user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }

    @Override
    @Transactional
    public ServiceResult modifyUserProfile(String profile, String value) {
        Long userId = LoginUserUtil.getLoginUserId();
        if (profile == null || profile.isEmpty()) {
            log.error("方法{};错误信息:{}", UserServiceImpl.class.getName()+"modifyUserProfile()", "属性不可以为空");
            return new ServiceResult(false, "属性不可以为空");
        }
        switch (profile) {
            case "name":
                userRepository.updateUsername(userId, value);
                break;
            case "email":
                userRepository.updateEmail(userId, value);
                break;
            case "password":
                //userRepository.updatePassword(userId, this.passwordEncoder.encodePassword(value, userId));
                userRepository.updatePassword(userId, this.passwordEncoder.encode(value));
                break;
            default:
                log.error("方法{};错误信息:{}", UserServiceImpl.class.getName()+"modifyUserProfile()", "不支持的属性");
                return new ServiceResult(false, "不支持的属性");
        }
        return ServiceResult.success();
    }
}
