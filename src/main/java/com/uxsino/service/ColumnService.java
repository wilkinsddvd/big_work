package com.uxsino.service;

import com.uxsino.DTO.AddColumnDTO;
import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.utils.LogConsts;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.entity.TableColumnDao;
import com.uxsino.entity.TableDao;
import com.uxsino.repository.TableColumnRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/31 10:15
 */
@Service
@Transactional
public class ColumnService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnService.class);

    @Autowired
    private TableColumnRepository tableColumnRepository;

    @Autowired
    private EntityManager entityManager;

    public Page<TableColumnDao> listColumn(PageListDTO tableDTO, Long tableId) {

        logger.debug(Constant.LIST_TABLE_MANAGER + "，请求参数为：" + tableDTO.toString());

        // 封装搜索字段
        PageRequest pageRequest = OperUtils.getPageRequest(tableDTO, Constant.COLUMN_MANAGER_ORDER_FIELDS);
        String[] searchFieldsArray = OperUtils.getSearchFieldsArray(tableDTO.getSearchFields());
        String[] searchArray = OperUtils.getSearchArray(tableDTO.getSearch());

        Page<TableColumnDao> page = tableColumnRepository.findAll(new Specification<TableColumnDao>() {
            @Override
            public Predicate toPredicate(Root<TableColumnDao> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                // 精准匹配
                predicates.add(builder.equal(root.get(Constant.TABLE_ID), tableId));
                // 如果筛选条件为空或长度不匹配，返回仅包含tableId的筛选
                if (searchFieldsArray == null || searchArray == null
                        || searchArray.length != searchFieldsArray.length) {
                    Predicate predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                    return query.where(predicate).getRestriction();
                }
                for (int i = 0; i < searchFieldsArray.length; i++) {
                    String key = searchFieldsArray[i];
                    if (Constant.COLUMN_NAME.equals(key) || Constant.COLUMN_DESC.equals(key)) {
                        // 模糊匹配
                        predicates.add(builder.like(root.get(key), LogConsts.WILDCARD + searchArray[i] + LogConsts.WILDCARD));
                    } else if (Constant.CONTROL_TYPE.equals(key) && !StringUtils.isEmpty(searchArray[i])) {
                        // 精准匹配
                        predicates.add(builder.equal(root.get(key), searchArray[i]));
                    }
                }
                Predicate predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                return query.where(predicate).getRestriction();
            }
        }, pageRequest);

        return page;
    }

    public Long save(AddColumnDTO columnDTO, TableDao tableDao) {
        // 封装成columnDao并保存
        TableColumnDao columnDao = TableColumnDao.builder().tableId(columnDTO.getTableId())
                .columnName(columnDTO.getColumnName())
                .columnDesc(columnDTO.getColumnDesc())
                .controlType(columnDTO.getControlType())
                .enumValues(columnDTO.getEnumValues())
                .displayMultiplier(columnDTO.getDisplayMultiplier())
                .table(tableDao).build();
        StringBuilder sql = new StringBuilder();
        // 如果id不为空，说明是修改操作
        if (columnDTO.getColumnId() != null) {
            // 获取之前的旧数据
            TableColumnDao columnDaoOld = findById(columnDTO.getColumnId());
            if (columnDaoOld == null) {
                throw new RuntimeException("要修改的列不存在！");
            }
            // 如果列名有修改，拼接语句并执行
            if (!columnDaoOld.getColumnName().equals(columnDao.getColumnName())) {
                /**
                 * 动态构造 SQL：ALTER TABLE public.tbl_test2 RENAME COLUMN "desc" TO "checkpoint";
                 * 列名添加双引号，防止出现数据库关键字导致报错
                 */
                sql = sql.append("ALTER TABLE public.\"").append(tableDao.getTableName()).append("\" RENAME COLUMN \"")
                        .append(columnDaoOld.getColumnName()).append("\" to \"").append(columnDao.getColumnName()).append("\";");
                // 在数据库中创建对应名称的表格
                entityManager.createNativeQuery(sql.toString()).executeUpdate();
            }
            columnDao.setColumnId(columnDTO.getColumnId());
        } else {
            // 否则是新建操作
            /**
             * 动态构造 SQL：ALTER TABLE public.tbl_test2 ADD COLUMN "column1" character varying NULL;
             * 列名添加双引号，防止出现数据库关键字导致报错
             */
            sql = sql.append("ALTER TABLE public.\"").append(tableDao.getTableName()).append("\" ADD COLUMN \"").append(columnDao.getColumnName())
                    .append("\" character varying NULL;");
            // 在数据库中创建对应名称的表格
            entityManager.createNativeQuery(sql.toString()).executeUpdate();
        }

        TableColumnDao tableColumnDao = tableColumnRepository.save(columnDao);
        return tableColumnDao.getColumnId();
    }

    public void save(TableColumnDao columnDao) {
        tableColumnRepository.save(columnDao);
    }

    public TableColumnDao findByColumnNameAndTableId(String columnName, Long tableId) {
        return tableColumnRepository.findByColumnNameAndTableId(columnName, tableId);
    }

    public TableColumnDao findById(Long columnId) {
        Optional<TableColumnDao> columnDao = tableColumnRepository.findById(columnId);
        return columnDao.orElse(null);
    }

    public void delete(TableColumnDao columnDao, TableDao tableDao) {
        // 拼接语句删除表中的列
        /**
         * 动态构造 SQL：ALTER TABLE public.tbl_test2 DROP COLUMN "checkpoint";
         * 列名添加双引号，防止出现数据库关键字导致报错
         */
        StringBuilder sql = new StringBuilder("ALTER TABLE public.\"").append(tableDao.getTableName()).append("\" DROP COLUMN \"")
                .append(columnDao.getColumnName()).append("\";");
        // 在数据库中创建对应名称的表格
        entityManager.createNativeQuery(sql.toString()).executeUpdate();
        tableColumnRepository.delete(columnDao);
    }
}
