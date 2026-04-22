package com.uxsino.service;

import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.utils.DbConfigUtil;
import com.uxsino.common.utils.EnumUtils;
import com.uxsino.common.utils.LogConsts;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.entity.User;
import com.uxsino.entity.WebLog;
import com.uxsino.repository.WebLogRepository;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * web操作日志服务层实现类
 *
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/04 14:55
 */
@Service
@Transactional
public class WebLogService {

    private static final Logger logger = LoggerFactory.getLogger(WebLogService.class);

    @Autowired
    private WebLogRepository webLogRepository;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private SimpleDateFormat dateFormatToStr = new SimpleDateFormat("yyyyMMdd");

    /**
     * 列的缺省值
     */
    private String DEFAULT = "";

    /**
     * 获取web操作日志列表事件的描述信息，用于日志输出和记录操作日志
     */
    public static String listEvent = LogConsts.LIST_WEB_LOG;

    public boolean success(String event) {
        Subject subject = SecurityUtils.getSubject();
        User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
        String operator = loginUser.getUserName();
        checkNotNull(operator, event);
        WebLog webLog = createWebLog(operator, event, EnumUtils.OperResult.SUCCESS, DEFAULT);
        return null != webLogRepository.save(webLog);
    }

    public boolean success(String operator, String event) {
        checkNotNull(operator, event);
        WebLog webLog = createWebLog(operator, event, EnumUtils.OperResult.SUCCESS, DEFAULT);
        return null != webLogRepository.save(webLog);
    }

    public boolean failed(String event) {
        Subject subject = SecurityUtils.getSubject();
        User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
        String operator = loginUser.getUserName();
        checkNotNull(operator, event);
        WebLog webLog = createWebLog(operator, event, EnumUtils.OperResult.FAILURE, DEFAULT);
        return null != webLogRepository.save(webLog);
    }

    public boolean failure(String event, String failedReason) {
        Subject subject = SecurityUtils.getSubject();
        User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
        String operator = loginUser.getUserName();
        checkNotNull(operator, event);
        WebLog webLog = createWebLog(operator, event, EnumUtils.OperResult.FAILURE, failedReason);
        return null != webLogRepository.save(webLog);
    }

    public boolean failed(String operator, String event) {
        checkNotNull(operator, event);
        WebLog webLog = createWebLog(operator, event, EnumUtils.OperResult.FAILURE, DEFAULT);
        return null != webLogRepository.save(webLog);
    }

    public boolean failed(String operator, String event, String failedReason) {
        checkNotNull(operator, event);
        WebLog webLog = createWebLog(operator, event, EnumUtils.OperResult.FAILURE, failedReason);
        return null != webLogRepository.save(webLog);
    }

    public Page<WebLog> listWebLog(PageListDTO webLogListDTO) {

        logger.debug(LogConsts.LIST_WEB_LOG + "，请求参数为：" + webLogListDTO.toString());

        // 封装搜索字段
        PageRequest pageRequest = OperUtils.getPageRequest(webLogListDTO, LogConsts.WEB_LOG_ORDER_FIELDS);
        String[] searchFieldsArray = OperUtils.getSearchFieldsArray(webLogListDTO.getSearchFields());
        String[] searchArray = OperUtils.getSearchArray(webLogListDTO.getSearch());

        // 分页查询
        Page<WebLog> page = webLogRepository.findAll(new Specification<WebLog>() {
            @Override
            public Predicate toPredicate(Root<WebLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (searchFieldsArray == null || searchArray == null
                        || searchArray.length != searchFieldsArray.length) {
                    return query.getRestriction();
                }
                Date startDate = null;
                Date endDate = null;
                for (int i = 0; i < searchFieldsArray.length; i++) {
                    String key = searchFieldsArray[i];
                    if (LogConsts.ID.equals(key) || LogConsts.RESULT.equals(key)) {
                        predicates.add(builder.equal(root.get(key), searchArray[i]));
                    } else if (LogConsts.OPERATOR.equals(key) || LogConsts.EVENT.equals(key)
                            || LogConsts.FAILED_REASON.equals(key)) {
                        // 操作人、事件、失败原因：模糊查询匹配
                        predicates
                            .add(builder.like(root.get(key), LogConsts.WILDCARD + searchArray[i] + LogConsts.WILDCARD));
                    } else if (LogConsts.START_DATE.equals(key)) {
                        try {
                            startDate = dateFormat.parse(searchArray[i] + LogConsts.START_DATE_COMP);
                        } catch (ParseException e) {
                            // ignore
                        }
                    } else if (LogConsts.END_DATE.equals(key)) {
                        try {
                            endDate = dateFormat.parse(searchArray[i] + LogConsts.END_DATE_COMP);
                        } catch (ParseException e) {
                            // ignore
                        }
                    } else {
                    }
                }
                // 时间校验：有一个时间不为空时，需要增加时间过滤
                if (startDate != null || endDate != null) {
                    if (startDate == null) {
                        startDate = new Date();
                    }
                    if (endDate == null) {
                        endDate = new Date();
                    }
                    predicates.add(builder.between(root.<Date> get(LogConsts.OPERTIME), startDate, endDate));
                }
                Predicate predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                return query.where(predicate).getRestriction();
            }
        }, pageRequest);
        return page;
    }

    /**
     * 校验并获取排序字段的第一个字段
     *
     * @param orderFields
     * @param optionalOrderFields
     * @return
     */
    private String parseOrderField(String orderFields, String[] optionalOrderFields) {
        // 排序字段
        if (orderFields != null) {
            String[] orderFieldList = orderFields.split(",");
            List<String> optionalOrderFieldList = Arrays.asList(optionalOrderFields);
            for (String orderField : orderFieldList) {
                if (optionalOrderFieldList.contains(orderField)) {
                    return orderField;
                }
            }
        }
        return optionalOrderFields[0];
    }

    /**
     * 校验时间
     *
     * @param startDate
     * @param endDate
     * @return {开始时间,结束时间}
     * @throws ParseException
     */
    private String[] checkDate(String startDate, String endDate) throws ParseException {
        if (startDate != null && endDate != null) {
            startDate = startDate + LogConsts.START_DATE_COMP;
            endDate = endDate + LogConsts.END_DATE_COMP;
            // 校验startDate和endDate的大小
            // compareTo()方法的返回值，date1小于date2返回-1，date1大于date2返回1，相等返回0
            int compareTo = dateFormat.parse(startDate).compareTo(dateFormat.parse(endDate));
            if (compareTo != -1) {
                throw new RuntimeException("参数错误！开始时间必须小于结束时间");
            }
        } else {
            throw new RuntimeException("参数错误！开始时间或结束时间不能为空");
        }
        String[] dates = { startDate, endDate };
        return dates;
    }

    /**
     * 获取操作日志备份表的表名后缀
     *
     * @param startDate
     * @param endDate
     * @return
     */
    private String getSuffix(String startDate, String endDate) throws ParseException {
        // 将日期字符串由"2021-03-07 00:00:00"转换为"20210307"
        String startDateStr = dateFormatToStr.format(dateFormat.parse(startDate));
        String endDateStr = dateFormatToStr.format(dateFormat.parse(endDate));
        return "_" + startDateStr + "_" + endDateStr;
    }

    /**
     * 封装一个WebLog对象
     *
     * @param operator
     * @param event
     * @param result
     * @param failedReason
     * @return
     */
    private WebLog createWebLog(String operator, String event, EnumUtils.OperResult result, String failedReason) {
        WebLog webLog = new WebLog();
        webLog.setOperator(operator);
        webLog.setOpertime(new Date());
        webLog.setEvent(event);
        webLog.setResult(result.getValue());
        webLog.setFailedReason(failedReason);
        return webLog;
    }

    /**
     * 校验不能为空的字段
     *
     * @param operator
     * @param event
     */
    private void checkNotNull(String operator, String event) {
        if (operator == null || event == null) {
            throw new RuntimeException("操作日志的操作员和事件不能为空！");
        }
    }
}
