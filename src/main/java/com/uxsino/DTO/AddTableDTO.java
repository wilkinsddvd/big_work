package com.uxsino.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 添加表格
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/30 11:03
 */
@Data
public class AddTableDTO {

    private String tableId;

    @NotNull(message = "表格名称不能为空")
    private String tableName;

    @NotNull(message = "表格描述不能为空")
    private String tableDesc;

    private List<AddColumnDTO> columns;
}
