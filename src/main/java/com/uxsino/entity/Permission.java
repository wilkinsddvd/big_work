package com.uxsino.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 权限实体类
 */
@Getter
@Setter
@Entity
@Table(name = "tbl_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permissionId;

    private String permissionName;

    private String permission;
}
