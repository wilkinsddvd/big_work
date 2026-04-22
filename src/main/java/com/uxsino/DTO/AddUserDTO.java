package com.uxsino.DTO;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
public class AddUserDTO {
    @Pattern(regexp = "^[0-9a-zA-Z_]{4,20}$", message = "用户名由字母、数字或下划线组成，长度4~20位")
    @NotNull(message = "用户名不能为空")
    private String userName;

    @Pattern(regexp = "^(?![a-zA-Z0-9]+$)(?![^a-zA-Z/D]+$)(?![^0-9/D]+$).{8,20}$", message = "密码至少包含一个数字、字母及特殊字符，长度8~20位")
    @NotNull(message = "密码不能为空")
    private String password;

    @NotNull(message = "角色不能为空")
    private Integer role;

    private String oldPassword;
}
