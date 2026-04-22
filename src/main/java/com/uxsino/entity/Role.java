package com.uxsino.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * 角色实体类
 */

@Getter
@Setter
@Entity
@Table(name = "tbl_role")
public class Role {

    @Id
    private Integer roleId;

    private String roleName;

    private String roleDesc;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_role_permission", joinColumns = {
            @JoinColumn(name = "ROLE_ID", referencedColumnName = "roleId") }, inverseJoinColumns = {
                    @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "permissionId") })
    private Set<Permission> permissions;
}