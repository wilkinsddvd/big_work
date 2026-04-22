package com.uxsino.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 表结构与数据导入/导出包（JSON 文件内容）
 */
@Data
public class TableTransferPackageDTO {

    /**
     * 固定格式标识，用于快速判定文件类型
     */
    private String format;

    /**
     * 版本号，用于后续兼容演进
     */
    private Integer version;

    private Meta meta;

    private TableInfo table;

    private List<Column> columns;

    /**
     * 行数据：key 为 columnName；值统一按字符串处理（便于与当前表字段类型 character varying 对齐）
     * 允许出现 id，但导入时会被忽略。
     */
    private List<Map<String, Object>> data;

    @Data
    public static class Meta {
        private String exportedAt;
        private String exportedBy;
        private Source source;
    }

    @Data
    public static class Source {
        private String app;
        private String db;
    }

    @Data
    public static class TableInfo {
        private String tableName;
        private String tableDesc;
    }

    @Data
    public static class Column {
        private String columnName;
        private String columnDesc;
        private String displayMultiplier;
        private String controlType;
        private String enumValues;
    }

    @Data
    public static class ImportPrecheckResult {
        private String token;
        private String tableName;
        private Integer columnCount;
        private Integer rowCount;

    }
}

