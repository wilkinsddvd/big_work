package com.uxsino.repository;

import com.uxsino.entity.WebLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;

/**
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/04 15:22
 */
public interface WebLogRepository extends JpaRepository<WebLog, Integer>, JpaSpecificationExecutor<WebLog> {

    int deleteByOpertimeBetween(Date startDate, Date endDate);
}