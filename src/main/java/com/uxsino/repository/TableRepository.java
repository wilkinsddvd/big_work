package com.uxsino.repository;

import com.uxsino.entity.TableDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TableRepository extends JpaRepository<TableDao, Long>, JpaSpecificationExecutor<TableDao> {

    // 使用 JPQL 查询所有表及其关联的列信息
    @Query(value = "SELECT DISTINCT t FROM TableDao t LEFT JOIN FETCH t.tableColumnList",
            countQuery = "SELECT COUNT(t) FROM TableDao t")
    List<TableDao> findAllTablesWithColumns(Specification<TableDao> spec, Pageable pageable);

    TableDao findByTableId(Long tableId);

    List<TableDao> findByTableName(String tableName);

    List<TableDao> findByTableDesc(String tableDesc);
}
