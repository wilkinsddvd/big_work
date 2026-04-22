package com.uxsino.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 17:15
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_tables")
public class TableDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long tableId;

    /**
     * 表格名称，在数据库中使用的表名
     */
    @Column(nullable = false, name = "table_name")
    private String tableName;

    /**
     * 表格描述：简短描述，在普通用户页面上显示的描述信息
     */
    @Column(nullable = false, name = "table_desc")
    private String tableDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 创建人
     */
    @Column(nullable = false, name = "create_user")
    private String createUser;

    // 一对多映射，关联到 Columns 表
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("columnId ASC")
    private List<TableColumnDao> tableColumnList;
}
