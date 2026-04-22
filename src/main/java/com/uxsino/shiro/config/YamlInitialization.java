package com.uxsino.shiro.config;

import com.uxsino.common.constant.FileConstant;
import com.uxsino.common.utils.SpringContextHolder;
import com.uxsino.entity.Token;
import com.uxsino.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用于自动加载配置文件修改类
 */
@Slf4j
@Component
public class YamlInitialization implements ApplicationRunner {

    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    private static Integer cache_tokenTime = 0;
    private static Integer cache_sendAlarmTime = 0;

    private static TokenRepository tokenRepository = SpringContextHolder.getBean(TokenRepository.class);

    private static YamlConfig yamlConfig = SpringContextHolder.getBean(YamlConfig.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduleUpdateConf();
    }

    private void scheduleUpdateConf() {
        // 开启定时刷新内存中配置文件内容
        log.info("refresh config file start");
        exec.scheduleAtFixedRate(YamlInitialization::updateConfVal, 0, 10, TimeUnit.SECONDS);
    }

    private static void updateConfVal() {
        try {
            // 1. 更新时间
            Map map = yamlConfig.getYamlToMap(FileConstant.SYSTEM_CONFIG_FILE_PATH);
            Integer tokenExpireTime = (Integer) yamlConfig.getValue( "server.token.expire-time",map);
            if (Objects.nonNull(cache_tokenTime) && !Objects.equals(cache_tokenTime,tokenExpireTime)) {
                cache_tokenTime = tokenExpireTime;
                // 更新修改后的时间数据
                List<Token> allTokens = tokenRepository.findAll();
                Optional.ofNullable(allTokens).ifPresent(tokenList -> {
                    tokenList.forEach(itemToken -> {
                        Token token = tokenRepository.findByUserId(itemToken.getUserId().toString());
                        token.setExpireTime(LocalDateTime.now().plus(Duration.ofMillis(tokenExpireTime)));
                        token.setUpdateTime(LocalDateTime.now());
                        tokenRepository.saveAndFlush(token);
                    });
                });
            }
            Integer sendAlarmIntervalTime = (Integer) yamlConfig.getValue("uxwebconfig.timeserver.detectionTime", map);
            log.info("实时配置tokenExpireTime==" + tokenExpireTime+",sendAlarmIntervalTime==" + sendAlarmIntervalTime);
        } catch (Exception e) {
            log.error("更新配置文件异常",e);
        }
    }
}
