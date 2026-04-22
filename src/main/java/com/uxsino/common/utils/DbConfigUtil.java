package com.uxsino.common.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.xml配置文件中数据库的配置信息
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/10 09:33
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource.druid")
@Data
public class DbConfigUtil {

    private String url;

    private String username;

    private String password;

}