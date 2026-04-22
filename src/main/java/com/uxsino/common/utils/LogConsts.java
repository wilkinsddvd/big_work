package com.uxsino.common.utils;

import java.text.SimpleDateFormat;

/**
 * 日志模块相关常量
 *
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/08 11:38
 */
public class LogConsts {

    /**
     * Web操作日志可能用到的搜索字段
     **/
    public static String OPERATOR = "operator";

    public static String ID = "id";

    public static String START_DATE = "startDate";

    public static String END_DATE = "endDate";

    public static String EVENT = "event";

    public static String RESULT = "result";

    public static String FAILED_REASON = "failedReason";

    /** Web操作日志可能用到的搜索字段 **/

    /**
     * web操作日志备份文件列表可能用到的搜索字段
     **/
    public static String FILE_NAME = "fileName";
    /** web操作日志备份文件列表可能用到的搜索字段 **/

    /** wal日志列表可能用到的搜索字段 **/
    public static String INST_ID = "instId";

    public static String LOG_TYPE = "logType";

    public static String START_CREATE_TIME = "startCreateTime";

    public static String END_CREATE_TIME = "endCreateTime";
    /** wal日志列表可能用到的搜索字段 **/

    /**
     * web操作日志可供选择的排序字段
     */
    public static final String[] WEB_LOG_ORDER_FIELDS = { "opertime", "operator", "event", "result", "failedReason",
            "id" };

    /**
     * web操作日志时间搜索对应字段名
     */
    public static String OPERTIME = "opertime";

    public static String FILE_SIZE = "fileSize";

    public static String CREATE_TIME = "createTime";

    /**
     * web操作日志备份文件可供选择的排序字段
     */
    public static final String[] WEB_LOG_BACKUP_ORDER_FIELDS = { FILE_NAME, FILE_SIZE };

    /**
     * 搜索字段中，开始时间的补全
     */
    public static String START_DATE_COMP = " 00:00:00";

    /**
     * 搜索字段中，结束时间的补全
     */
    public static String END_DATE_COMP = " 23:59:59";

    /**
     * 搜索字段中，开始时间的补全
     */
    public static String DATE_COMP = ":00";

    /**
     * 左括号
     * <p>写入操作日志时标注变量使用</p>
     */
    public static String LEFT_BRACKET = "【";

    /**
     * 右括号
     * <p>写入操作日志时标注变量使用</p>
     */
    public static String RIGHT_BRACKET = "】";

    /**
     * 通配符
     */
    public static String WILDCARD = "%";

    /** 请求响应中部分通用常量 **/
    public static String QUERY_SUCCESS = "查询成功";
    public static String QUERY_FAILURE = "查询失败";

    public static String OPER_SUCCESS = "操作成功";

    /** 请求响应中部分通用常量 **/

    /**
     * 日期格式化
     */
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     * web操作日志备份数据表名
     */
    public static String WEBLOG_TMP_TABLE_NAME = "tbl_weblog_tmp";

    /**
     * web操作日志备份文件名
     */
    public static String WEBLOG_FILE_NAME = "weblog";

    /**
     * web操作日志备份文件后缀
     */
    public static String BACKFILE_SUFFIX = ".tar.gz";

    /**
     * 文件大小单位换算间隔
     */
    public static int UNIT = 1024;

    /**
     * 文件大小单位
     */
    public static String[] UNITS = { "Bytes", "KB", "MB", "GB", "TB" };

    /**
     * 除法保留两位小数
     */
    public static double PRECISION = 100.0;

    /**
     * 获取文件url结果字段
     */
    public static String URL = "url";

    /**
     * web操作日志备份文件可供选择的排序字段
     */
    public static final String[] WAL_LOG_ORDER_FIELDS = { FILE_NAME, FILE_SIZE, CREATE_TIME };

    /**
     * webAgent中下载服务的端口
     */
    public static final String DOWNLOAD_SERVER = "10008";

    /**
     * createtime转date时，需要乘以1000再转换
     */
    public static final long TIME_TO_DATE = 1000;

    /** wal日志和数据库日志接口 **/
    public static final String LIST_WAL_OR_DB_LOG = "获取wal日志/数据库日志列表";
    public static final String EXPORT_WAL_OR_DB_LOG = "导出wal日志/数据库日志列表";
    public static final String DELETE_DB_LOG = "删除数据库日志";

    /** web操作日志接口 **/
    public static final String LIST_WEB_LOG = "获取web操作日志列表";
    public static final String BACKUP_WEB_LOG = "备份web操作日志";
    public static final String EXPORT_WEB_LOG = "导出web操作日志备份文件";
    public static final String LIST_BACKUP_WEB_LOG = "获取web操作日志备份文件列表";
    public static final String DELETE_BACKUP_WEB_LOG = "删除web操作日志备份文件";
}