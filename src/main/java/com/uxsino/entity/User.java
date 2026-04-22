package com.uxsino.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 11:09
 */
@Getter
@Setter
@Entity
@Table(name = "tbl_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    private String salt;

    private String createUser;

    private Integer groupId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_user_role", joinColumns = {
            @JoinColumn(name = "USER_ID", referencedColumnName = "userId") }, inverseJoinColumns = {
            @JoinColumn(name = "ROLE_ID", referencedColumnName = "roleId") })
    private Set<Role> roles;

    // 重新对盐重新进行了定义，用户名+salt，这样就更加不容易被破解
    public String getCredentialsSalt() {
        return this.getUserName() + this.getSalt();
    }
}
