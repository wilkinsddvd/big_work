package com.uxsino.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @className LoginVO
 * @description TODO
 * @date 2021/2/8 11:46
 */
@Data
public class LoginVO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expire;

    private String token;

    private Integer roleId;

    private String userName;

    private String serverPlatform;
}
