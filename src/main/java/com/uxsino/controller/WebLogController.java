package com.uxsino.controller;

import com.uxsino.DTO.ListWebLogVO;
import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.response.ResultInfo;
import com.uxsino.common.response.ResultPage;
import com.uxsino.common.utils.LogConsts;
import com.uxsino.entity.WebLog;
import com.uxsino.service.WebLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * web操作日志控制层
 *
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/04 10:36
 */
@RestController
@RequestMapping("/weblog")
public class WebLogController {

    private static final Logger logger = LoggerFactory.getLogger(WebLogController.class);

    @Autowired
    private WebLogService webLogService;

    @GetMapping
    public ResultInfo listWebLog(PageListDTO webLogListDTO) {
        try {
            Page<WebLog> pageWebLogs = webLogService.listWebLog(webLogListDTO);
            // 分页视图结果封装
            List<ListWebLogVO> content = new ArrayList<>();
            List<WebLog> webLogs = pageWebLogs.getContent();
            ListWebLogVO listWebLogVO = null;
            for (WebLog webLog : webLogs) {
                listWebLogVO = new ListWebLogVO();
                listWebLogVO.setOperator(webLog.getOperator());
                listWebLogVO.setOperTime(webLog.getOpertime());
                listWebLogVO.setEvent(webLog.getEvent());
                listWebLogVO.setResult(webLog.getResult());
                listWebLogVO.setFailedReason(webLog.getFailedReason());
                content.add(listWebLogVO);
            }
            ResultPage<ListWebLogVO> resultPage = ResultPage.restPage(pageWebLogs, content);
            return ResultInfo.success(LogConsts.QUERY_SUCCESS, resultPage);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + WebLogService.listEvent + "，请求参数：" + webLogListDTO.toString(), e);
            return ResultInfo.failed(e.getMessage());
        }
    }
}