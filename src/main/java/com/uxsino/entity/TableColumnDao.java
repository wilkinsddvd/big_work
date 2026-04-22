package com.uxsino.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

/**
 * 表对应列的实体类
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/23 14:24
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_columns")
public class TableColumnDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long columnId;

    /**
     * 外键，关联到tbl_tables表
     */
    @Column(nullable = false, name = "table_id", insertable = false, updatable = false)
    private Long tableId;

    /**
     *
     */
    @Column(nullable = false, name = "column_name")
    private String columnName;

    /**
     *
     */
    @Column(nullable = false, name = "column_desc")
    private String columnDesc;

    /**
     *
     */
    @Column(nullable = false, name = "display_multiplier")
    private String displayMultiplier;

    /**
     *
     */
    @Column(nullable = false, name = "control_type")
    private String controlType;

    /**
     * 如果是枚举类型，需要有对应枚举值
     */
    @Column(name = "enum_values")
    private String enumValues;

    // 多对一映射，关联到 Tables 表
    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    @JsonIgnore
    private TableDao table;
}
