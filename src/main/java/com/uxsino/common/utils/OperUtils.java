package com.uxsino.common.utils;

import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.response.ResultInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/05 16:25
 */
public class OperUtils {

    private static Integer DEFAULT_PAGE_NUM = 1;

    private static Integer DEFAULT_PAGE_SIZE = 300;

    private static String DESC = "desc";

    /**
     * 封装PageRequest
     * @param pageListDTO 请求中的pageListDTO参数
     * @param optionalOrderFields 当前查询可供选择的排序字段
     * @return
     */
    public static PageRequest getPageRequest(PageListDTO pageListDTO, String[] optionalOrderFields) {
        // 参数校验
        int pageNum = checkPageNum(pageListDTO.getPackageNum());
        int pageSize = checkPageSize(pageListDTO.getPackageSize());
        String[] orderFieldsArray = getOrderFieldsArray(pageListDTO.getOrderFields(), optionalOrderFields);
        // 排序order
        Sort.Direction finalOrder = getOrder(pageListDTO.getOrder());

        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, finalOrder, orderFieldsArray);
        return pageRequest;
    }

    public static String[] getSearchFieldsArray(String searchFields) {
        return (StringUtils.isEmpty(searchFields)) ? null : searchFields.split(",");
    }

    public static String[] getSearchArray(String search) {
        // 搜索值，split接口默认忽略末尾的空字符串，使用参数limit=-1后，可以将末尾的空字符串作为数组的一个元素返回，举例："a,b,"，limit=-1后返回三个元素
        return (StringUtils.isEmpty(search)) ? null : search.split(",", -1);
    }

    public static String[] getOrderFieldsArray(String orderFields, String[] optionalOrderFields) {
        // 字段处理，去除字符串参数左右的单引号
        if (orderFields.startsWith("'")) {
            orderFields = orderFields.substring(1);
        }
        if (orderFields.endsWith("'")) {
            orderFields = orderFields.substring(0, orderFields.length() - 1);
        }
        // 排序字段
        // 筛选每一个字段，看是否在可选排序字段里面
        if (StringUtils.isEmpty(orderFields)) {
            return new String[] { optionalOrderFields[0] };
        }
        List<String> optionalOrderFieldList = Arrays.asList(optionalOrderFields);
        String[] splits = orderFields.split(",");
        String[] orderFieldsArray = new String[splits.length];
        int i = 0;
        for (String split : splits) {
            if (optionalOrderFieldList.contains(split)) {
                orderFieldsArray[i] = split;
                i++;
            } else {
                // 包含不允许的排序字段
                throw new RuntimeException("参数错误：字段orderFields包含不允许的排序字段！");
            }
        }
        return orderFieldsArray;
    }

    /**
     * 校验并获取排序字段列表
     *
     * @param orderFields
     * @param optionalOrderFields
     * @return
     */
    public static List<String> parseOrderField(String orderFields, String[] optionalOrderFields) {
        List<String> orderFieldArray = new ArrayList<>();
        // 排序字段
        if (orderFields != null) {
            String[] orderFieldList = orderFields.split(",");
            List<String> optionalOrderFieldList = Arrays.asList(optionalOrderFields);
            for (String orderField : orderFieldList) {
                if (optionalOrderFieldList.contains(orderField)) {
                    orderFieldArray.add(orderField);
                }
            }
        }
        return orderFieldArray;
    }

    public static Sort.Direction getOrder(String order) {
        // 排序order
        Sort.Direction finalOrder = Sort.Direction.ASC;
        if (order != null && DESC.equalsIgnoreCase(order)) {
            finalOrder = Sort.Direction.DESC;
        }
        return finalOrder;
    }

    public static boolean orderIsAsc(String order) {
        // 排序order
        boolean isAsc = true;
        if (order != null && DESC.equalsIgnoreCase(order)) {
            isAsc = false;
        }
        return isAsc;
    }

    public static Integer checkPageNum(Integer pageNum) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    public static Integer checkPageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 转换单位：将传入的数字由比特向上转换
     * @param bytes
     * @return
     */
    public static String parseSize(long bytes) {
        double result = bytes;
        int i = 0;
        boolean end = true;
        while (end) {
            if (result >= LogConsts.UNIT) {
                result = Math.round(result / LogConsts.UNIT * LogConsts.PRECISION) / LogConsts.PRECISION;
                i++;
            } else {
                end = false;
            }
        }
        return result + LogConsts.UNITS[i];
    }

    public static String parseArg(String arg) {
        return LogConsts.LEFT_BRACKET + arg + LogConsts.RIGHT_BRACKET;
    }

    public static void checkArgs(List<String> stringList) {
        for (String str : stringList) {
            if (str == null) {
                throw new RuntimeException("参数中存在空值！");
            }
        }
        if (stringList == null || stringList.size() == 0) {
            throw new RuntimeException("参数列表为空！");
        }
    }

    public static void checkArgs(String[] args, String[] descs) {
        if (args == null || descs == null || args.length != descs.length) {
            throw new RuntimeException("函数OperUtils.checkArgs()的入参有误，请确认参数args和descs均不为空且长度相同！");
        }
        for (int i = 0; i < args.length; i++) {
            if (StringUtils.isEmpty(args[i])) {
                throw new RuntimeException(descs[i]);
            }
        }
    }

    public static void permissionCheck(String role) {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.hasRole(role)) {
            throw new RuntimeException("无权限！");
        }
    }

    /**
     * 解析hibernate返回的USQLException的message信息
     * @param e
     * @return
     */
    public static String parseException(Exception e) {
        // 回显hibernate返回的USQLException的message信息
        if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof SQLException) {
            return e.getCause().getCause().getMessage();
        }
        return e.getMessage();
    }
}