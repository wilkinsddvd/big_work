package com.uxsino.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/30 11:04
 */
@Data
public class AddColumnDTO {

    private Long columnId;

    private Long tableId;

    @NotNull(message = "列名不能为空")
    private String columnName;

    @NotNull(message = "列描述不能为空")
    private String columnDesc;

    @NotNull(message = "列宽倍数不能为空")
    private String displayMultiplier;

    @NotNull(message = "列组件类型不能为空")
    private String controlType;

    private String enumValues;
}
