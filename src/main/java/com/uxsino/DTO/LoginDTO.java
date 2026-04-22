package com.uxsino.DTO;

import lombok.Data;

/**
 * 登录传输类
 */
@Data
public class LoginDTO {
    //@NotBlank(message = "用户名不能为空")
    private String userName;

    //@NotBlank(message = "密码不能为空")
    private String password;
}
