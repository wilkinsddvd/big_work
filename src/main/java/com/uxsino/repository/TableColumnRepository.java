package com.uxsino.repository;

import com.uxsino.entity.TableColumnDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2025/01/02 17:13
 */
public interface TableColumnRepository extends JpaRepository<TableColumnDao, Long>, JpaSpecificationExecutor<TableColumnDao> {

    TableColumnDao findByColumnNameAndTableId(String columnName, Long tableId);
}
