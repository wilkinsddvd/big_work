package com.uxsino.shiro.service;

import com.uxsino.entity.Token;
import com.uxsino.entity.User;

import java.util.Map;

public interface ShiroService {
    /**
     * Find user by username
     *
     * @param userName
     * @return
     */
    User findByUserName(String userName);

    /**
     * create token by userId
     *
     * @param userId
     * @return
     */
    Map<String, Object> createToken(Integer userId);

    /**
     * logout
     *
     * @param token
     */
    void logout(String token);

    /**
     * find token by token
     *
     * @param accessToken
     * @return
     */
    Token findByToken(String accessToken);

    /**
     * find user by userId
     *
     * @param userId
     * @return
     */
    User findByUserId(Integer userId);
}
