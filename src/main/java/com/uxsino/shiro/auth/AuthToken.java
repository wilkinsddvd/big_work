package com.uxsino.shiro.auth;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Shiro自定义token类
 */
public class AuthToken extends UsernamePasswordToken {

    private String token;

    public AuthToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
