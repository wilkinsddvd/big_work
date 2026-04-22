package com.uxsino.common.utils;

/**
 * 枚举类型的工具类
 *
 * @version webServer 2.1.0.1
 * Copyright 2021 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2021/03/04 15:29
 */
public class EnumUtils {

    /**
     * web日志操作结果
     */
    public enum OperResult {

        /**
         * 操作成功
         */
        SUCCESS("success"),

        /**
         * 操作失败
         */
        FAILURE("failure");

        private final String value;

        OperResult(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 日志类型：数据库/wal日志
     */
    public enum LogType {

        /**
         * 数据库日志
         */
        DB_LOG(1, "数据库日志"),

        /**
         * wal日志
         */
        WAL_LOG(2, "wal日志");

        private final int value;

        private final String desc;

        LogType(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public static LogType parse(int value) {
            if (value == DB_LOG.getValue()) {
                return DB_LOG;
            } else if (value == WAL_LOG.getValue()) {
                return WAL_LOG;
            } else {
                throw new RuntimeException("value无效");
            }
        }
    }

    /**
     * webagent返回的结果码
     */
    public enum RedCode {

        /**
         * 成功
         */
        SUCCESS(0),

        /**
         * 失败
         */
        FAILURE(1);

        private final int value;

        RedCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}