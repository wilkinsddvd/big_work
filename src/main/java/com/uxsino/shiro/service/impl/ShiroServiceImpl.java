package com.uxsino.shiro.service.impl;

import com.uxsino.common.constant.FileConstant;
import com.uxsino.entity.Token;
import com.uxsino.entity.User;
import com.uxsino.repository.TokenRepository;
import com.uxsino.repository.UserRepository;
import com.uxsino.shiro.auth.TokenGenerator;
import com.uxsino.shiro.config.YamlConfig;
import com.uxsino.shiro.service.ShiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShiroServiceImpl implements ShiroService {
    // 默认30分失效，毫秒为单位
    @Value("${server.token.expire-time}")
    private int tokenExpireTime;

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    @Autowired
    private YamlConfig yamlConfig;

    public ShiroServiceImpl(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * 根据username查找用户
     *
     * @param userName
     * @return User
     */
    @Override
    public User findByUserName(String userName) {
        User user = userRepository.findByUserName(userName);
        return user;
    }

    @Override
    /**
     * 生成一个token
     *@param  [userId]
     *@return Result
     */
    public Map<String, Object> createToken(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        // 生成一个token
        String token = TokenGenerator.generateValue();
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 过期时间
        Map map = yamlConfig.getYamlToMap(FileConstant.SYSTEM_CONFIG_FILE_PATH);
        Integer tokenExpireTimeOut = (Integer) yamlConfig.getValue("server.token.expire-time", map);
        LocalDateTime expireTime = now.plus(Duration.ofMillis(tokenExpireTimeOut));
        // 判断是否生成过token
        Token tokenEntity = tokenRepository.findByUserId(userId.toString());
        if (tokenEntity == null) {
            tokenEntity = new Token();
            tokenEntity.setUserId(userId.toString());
            // 保存token
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);
        } else {
            // 更新token
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);
        }
        tokenRepository.save(tokenEntity);
        result.put("token", token);
        result.put("expire", expireTime);
        return result;
    }

    /**
     * 更新数据库的token，使前端拥有的token失效
     * 防止黑客利用token搞事情
     *
     * @param token
     */
    @Override
    public void logout(String token) {
        Token byToken = findByToken(token);
        // 生成一个token
        token = TokenGenerator.generateValue();
        // 修改token
        byToken.setToken(token);
        // 使前端获取到的token失效
        tokenRepository.save(byToken);
    }

    @Override
    public Token findByToken(String accessToken) {
        return tokenRepository.findByToken(accessToken);
    }

    @Override
    public User findByUserId(Integer userId) {
        return userRepository.findByUserId(userId);
    }
}
