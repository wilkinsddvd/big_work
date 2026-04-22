package com.uxsino.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/19 09:20
 */
@Data
@Builder
public class TableVO {

    private Long tableId;

    /**
     * 表格名称，在数据库中使用的表名
     */
    private String tableName;

    /**
     * 表格描述：简短描述，在普通用户页面上显示的描述信息
     */
    private String tableDesc;

    /**
     * 列的列表，保存当前表格的所有列，使用引文逗号分隔，与列类型和列描述一一对应
     */
    private List<String> columnList;

    /**
     * 列的描述，在页面上表格的表头中显示，使用引文逗号分隔，与列类型和列描述一一对应
     */
    private List<String> columnDescList;

    /**
     * 列的描述，在页面上表格的表头中显示，使用引文逗号分隔，与列类型和列描述一一对应
     */
    private List<String> columnWidthMultipleList;
}
