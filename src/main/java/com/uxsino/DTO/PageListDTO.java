package com.uxsino.DTO;

import lombok.Data;

/**
 * @className PageListDTO
 * @description 分页查询DTO
 * @date 2021/1/21 17:38
 */
@Data
public class PageListDTO {
    private Integer packageNum;

    private Integer packageSize;

    private String orderFields;

    private String order;

    private String searchFields;

    private String search;

    private String tableName;
}
