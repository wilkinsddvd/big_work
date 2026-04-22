package com.uxsino.controller;

import com.uxsino.DTO.AddColumnDTO;
import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.response.ResultInfo;
import com.uxsino.common.response.ResultPage;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.entity.TableColumnDao;
import com.uxsino.entity.TableDao;
import com.uxsino.service.ColumnService;
import com.uxsino.service.TableService;
import com.uxsino.service.WebLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/31 10:14
 */
@RestController
@RequestMapping("/column")
public class ColumnController {

    private static final Logger logger = LoggerFactory.getLogger(ColumnController.class);

    @Autowired
    private ColumnService columnService;

    @Autowired
    private TableService tableService;

    @Autowired
    private WebLogService webLogService;

    @GetMapping("/list")
    public ResultInfo listColumn(PageListDTO columnDTO, @RequestParam Long tableId) {
        try {
            Page<TableColumnDao> pageTables = columnService.listColumn(columnDTO, tableId);
            if (null == pageTables) {
                return ResultInfo.failed("查询失败");
            }

            ResultPage<TableColumnDao> resultPage = ResultPage.restPage(pageTables, pageTables.getContent());
            return ResultInfo.success("查询成功", resultPage);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.LIST_COLUMN_MANAGER + "，请求参数：tableId: " + tableId + columnDTO.toString(), e);
            return recordLogAndReturn(Constant.LIST_COLUMN_MANAGER + "，tableId: " + tableId, e.getMessage());
        }
    }

    /**
     * 保存/修改数据
     * <p>备注：新增和修改均使用此接口</p>
     * @param columnDTO
     * @return
     */
    @PostMapping("/save")
    public ResultInfo save(@RequestBody AddColumnDTO columnDTO) {
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);

            TableDao tableDao = findByTableId(Constant.DELETE_COLUMN, columnDTO.getTableId());
            TableColumnDao columnDao = columnService.findByColumnNameAndTableId(columnDTO.getColumnName(), columnDTO.getTableId());
            // 列id不为空，表示为修改操作，不用判断字段是否已存在
            if (columnDTO.getColumnId() == null) {
                if (columnDao != null) {
                    return recordLogAndReturn(Constant.SAVE_COLUMN, "字段已存在！");
                }
            }
            Long columnId = columnService.save(columnDTO, tableDao);

            webLogService.success(Constant.SAVE_COLUMN + "，columnId is " + columnId);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.SAVE_COLUMN + "，请求参数：" + columnDTO, e);
            // 回显hibernate返回的USQLException的message信息
            return recordLogAndReturn(Constant.SAVE_COLUMN, OperUtils.parseException(e));
        }
    }

    @PostMapping("/delete")
    public ResultInfo delete(@RequestParam @NotNull Long columnId, @RequestParam @NotNull Long tableId) {
        String event = Constant.DELETE_COLUMN + "，columnId is " + columnId;
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);
            // 校验对应表格是否存在
            TableDao tableDao = findByTableId(Constant.DELETE_COLUMN, tableId);
            // 校验字段是否存在
            TableColumnDao columnDao = columnService.findById(columnId);
            if (columnDao == null) {
                return recordLogAndReturn(event, "字段不存在！");
            }
            // id不能被删除
            if ("id".equals(columnDao.getColumnName())) {
                return recordLogAndReturn(event, "id字段不能被删除！");
            }

            columnService.delete(columnDao, tableDao);

            webLogService.success(event);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event + ", tableId: " + tableId, e);
            // 回显hibernate返回的USQLException的message信息
            return recordLogAndReturn(event, e.getMessage());
        }
    }

    @PostMapping("/batchDelete")
    public ResultInfo batchDelete(@RequestBody @NotNull List<Long> recordIdList, @RequestParam @NotNull Long tableId) {
        String event = Constant.BATCH_DELETE_COLUMN + ", recordIdList is " + Arrays.toString(recordIdList.toArray());
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);
            TableDao tableDao = findByTableId(event, tableId);
            for (Long columnId : recordIdList) {
                // 列id不为空，表示为修改操作，不用判断字段是否已存在
                TableColumnDao columnDao = columnService.findById(columnId);
                if (columnDao == null) {
                    return recordLogAndReturn(event, "字段不存在！");
                }
                // id不能被删除
                if ("id".equals(columnDao.getColumnName())) {
                    return recordLogAndReturn(Constant.DELETE_COLUMN, "id字段不能被删除！");
                }
                columnService.delete(columnDao, tableDao);
            }

            webLogService.success(event);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event + ", tableId: " + tableId, e);
            // 回显hibernate返回的USQLException的message信息
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    /**
     * 记录日志并返回失败对象
     *
     * @param event 事件描述
     * @param errorMessage 错误信息
     * @return ResultInfo
     */
    private ResultInfo recordLogAndReturn(String event, String errorMessage) {
        // 记录操作日志
        webLogService.failure(event, errorMessage);
        return ResultInfo.failed(errorMessage);
    }

    public TableDao findByTableId(String event, Long tableId) {
        TableDao tableDao = tableService.findByTableId(tableId);
        if (tableDao == null) {
            webLogService.failed(event);
            throw new RuntimeException("对应表格不存在！");
        }
        return tableDao;
    }
}
