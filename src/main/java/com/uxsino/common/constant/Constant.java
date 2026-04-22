package com.uxsino.common.constant;

/**
 *
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/19 09:24
 */
public class Constant {

    /** 表管理接口 **/
    public static final String LIST_TABLE_MANAGER = "获取表格列表";
    public static final String SAVE_TABLE = "保存表格信息";
    public static final String UPDATE_TABLE = "更新表格信息";
    public static final String DELETE_TABLE = "删除表格";
    public static final String BATCH_DELETE_TABLE = "批量删除表格";

    /** 表数据接口 **/
    public static final String LIST_TABLE_DATA = "获取表的数据";
    public static final String SAVE_TABLE_DATA = "保存表的数据";
    public static final String UPDATE_TABLE_DATA = "更新表的数据";
    public static final String DELETE_TABLE_DATA = "删除表的数据";
    public static final String BATCH_DELETE_TABLE_DATA = "批量删除表的数据";
    public static final String BATCH_UPDATE_TABLE_DATA = "批量更新表的数据";

    /** 列管理接口 **/
    public static final String LIST_COLUMN_MANAGER = "获取字段列表";
    public static final String SAVE_COLUMN = "保存字段信息";
    public static final String UPDATE_COLUMN = "更新字段信息";
    public static final String DELETE_COLUMN = "删除字段";
    public static final String BATCH_DELETE_COLUMN = "批量删除字段";
    public static final String BATCH_UPDATE_COLUMN = "批量更新字段数据";

    /** 用户管理接口 **/
    public static final String LIST_USER_MANAGER = "获取用户列表";
    public static final String SAVE_USER_DATA = "保存用户数据";
    public static final String UPDATE_USER_DATA = "更新用户数据";
    public static final String DELETE_USER_DATA = "删除用户数据";
    public static final String BATCH_UPDATE_USER_DATA = "批量更新用户数据";
    public static final String BATCH_DELETE_USER_DATA = "批量删除用户数据";
    public static final String RESET_USER_PWD = "修改用户密码";

    public static String OPER_SUCCESS = "操作成功";

    /**
     * 表管理可供选择的排序字段
     */
    public static final String[] TABLE_MANAGER_ORDER_FIELDS = {"tableId", "tableName"};

    /**
     * 用户可供选择的排序字段
     */
    public static final String[] USER_MANAGER_ORDER_FIELDS = {"userId"};

    /**
     * 表管理可供选择的排序字段
     */
    public static final String[] COLUMN_MANAGER_ORDER_FIELDS = {"columnId"};

    // 系统管理员
    public static final String SUPER = "super";
    // 普通用户
    public static final String USER = "user";
    // 超级管理员
    public static final String ADMIN = "admin";

    /** 用户管理可供筛选字段 **/
    // 普通用户
    public static final String USER_NAME = "userName";
    // 超级管理员
    public static final String ROLE_ID = "roleId";

    /** 列管理可供筛选字段 **/
    public static final String TABLE_ID = "tableId";
    public static final String COLUMN_NAME = "columnName";
    public static final String COLUMN_DESC = "columnDesc";
    public static final String CONTROL_TYPE = "controlType";

    /** 表格管理可供筛选字段 **/
    public static final String TABLE_NAME = "tableName";
    public static final String TABLE_DESC = "tableDesc";
    public static final String CREATE_USER = "createUser";
    public static final String CREATE_TIME = "createTime";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
}
