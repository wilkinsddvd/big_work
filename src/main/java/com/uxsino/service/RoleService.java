package com.uxsino.service;

import com.uxsino.entity.Role;
import com.uxsino.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className RoleServiceImpl
 * @description
 * @date 2021/1/21 12:46
 */
@Service
public class RoleService {
    @Autowired
    RoleRepository roleRepository;

    public Role findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    public List<Role> listRole() {
        return roleRepository.findAll();
    }

    public Role findByRoleId(Integer roleId) {
        return roleRepository.findById(roleId).orElse(null);
    }
}
