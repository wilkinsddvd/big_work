package com.uxsino.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxsino.DTO.AddTableDTO;
import com.uxsino.DTO.PageListDTO;
import com.uxsino.DTO.TableTransferPackageDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.response.ResultInfo;
import com.uxsino.common.response.ResultPage;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.entity.TableDao;
import com.uxsino.entity.User;
import com.uxsino.service.TableService;
import com.uxsino.service.WebLogService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 表相关操作的控制器
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 17:13
 */
@RestController
@RequestMapping("/table")
public class TableController {

    private static final Logger logger = LoggerFactory.getLogger(TableController.class);

    @Autowired
    private TableService tableService;

    @Autowired
    private WebLogService webLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/list")
    public ResultInfo listTable(PageListDTO tableDTO) {
        String event = Constant.LIST_TABLE_MANAGER + "，请求参数：" + tableDTO.toString();
        try {
            Page<TableDao> pageTables = tableService.listTable(tableDTO);
            if (null == pageTables) {
                return ResultInfo.failed("查询失败");
            }

            ResultPage<TableDao> resultPage = ResultPage.restPage(pageTables, pageTables.getContent());
            return ResultInfo.success("查询成功", resultPage);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event, e);
            return recordLogAndReturn(event, e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResultInfo save(@RequestBody AddTableDTO addTableDTO) {
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);
            // 校验字段信息
            OperUtils.checkArgs(new String[]{addTableDTO.getTableName(), addTableDTO.getTableDesc()}
                , new String[]{"表名不能为空！", "表描述不能为空"});

            // 登录用户信息
            Subject subject = SecurityUtils.getSubject();
            // 登录用户信息
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
            // 保存tableDao并获取保存后的Dao，后续保存列数据时需要使用其tableId
            tableService.save(addTableDTO, loginUser.getUserName());

            webLogService.success(Constant.SAVE_TABLE);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.SAVE_TABLE + "，请求参数：" + addTableDTO, e);
            // 回显hibernate返回的USQLException的message信息
            return recordLogAndReturn(Constant.SAVE_TABLE + "，请求参数：" + addTableDTO, OperUtils.parseException(e));
        }
    }

    @PostMapping("/update")
    public ResultInfo update(@RequestBody AddTableDTO addTableDTO) {
        String event = Constant.UPDATE_TABLE + ", tableId is " + addTableDTO.getTableId();
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);

            TableDao tableDao = tableService.findByTableId(Long.parseLong(addTableDTO.getTableId()));
            if (tableDao == null) {
                // 如果表格不存在
                return recordLogAndReturn(event, "表格不存在");
            }
            tableDao.setTableName(addTableDTO.getTableName());
            tableDao.setTableDesc(addTableDTO.getTableDesc());
            // 保存tableDao并获取保存后的Dao，后续保存列数据时需要使用其tableId
            boolean isSuccess = tableService.save(tableDao);
            return isSuccess ? recordSuccessLogAndReturn(event) : recordFailedLogAndReturn(event);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.UPDATE_TABLE + "，请求参数：" + addTableDTO, e);
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @PostMapping("/delete")
    public ResultInfo delete(@RequestParam Long tableId) {
        String event = Constant.DELETE_TABLE + ", tableId is " + tableId;
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);

            // 校验参数
            if (tableId == null) {
                return ResultInfo.failed("参数错误");
            }

            // 获取table
            TableDao tableDao = tableService.findByTableId(tableId);
            if (tableDao == null) {
                return recordLogAndReturn(Constant.DELETE_TABLE, "该表格不存在");
            }

            tableService.delete(tableDao);

            webLogService.success(event);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event, e);
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @PostMapping("/batchDelete")
    public ResultInfo batchDelete(@RequestBody List<String> tableIdList) {
        String event = Constant.BATCH_DELETE_TABLE + ", tableIdList is " + Arrays.toString(tableIdList.toArray());
        try {
            // 权限校验：表格的信息增删改只能由系统管理员处理
            OperUtils.permissionCheck(Constant.SUPER);

            for (String tableId : tableIdList) {
                // 校验参数
                if (tableId == null) {
                    return ResultInfo.failed("参数错误");
                }

                // 获取table
                TableDao tableDao = tableService.findByTableId(Long.parseLong(tableId));
                if (tableDao == null) {
                    return recordLogAndReturn(Constant.DELETE_TABLE, "该表格不存在");
                }

                tableService.delete(tableDao);
            }

            webLogService.success(event);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event, e);
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @GetMapping("/getTableData")
    public ResultInfo datalist(PageListDTO tableDTO) {
        try {
            ResultPage<Map<String, Object>> tableDataList = tableService.getTableData(tableDTO);
            if (null == tableDataList) {
                return ResultInfo.failed("查询失败");
            }
            return ResultInfo.success("查询成功", tableDataList);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.LIST_TABLE_DATA + "，请求参数：" + tableDTO.toString(), e);
            // 回显hibernate返回的USQLException的message信息，若报错不是USQLException，则返回e.getMessage()信息
            return recordLogAndReturn(Constant.LIST_TABLE_DATA, OperUtils.parseException(e));
        }
    }

    @PostMapping("/saveData")
    public ResultInfo saveData(@RequestParam String tableName, @RequestBody Map<String, String> data) {
        String event = Constant.SAVE_TABLE_DATA + ", tableName is " + tableName;
        try {
            boolean isSuccess = tableService.saveData(tableName, data);
            return isSuccess ? recordSuccessLogAndReturn(event) : recordFailedLogAndReturn(event);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.SAVE_TABLE_DATA + "，请求参数：tableName:" + tableName + ", data:" + data.toString(), e);
            // 回显hibernate返回的USQLException的message信息
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @PostMapping("/updateData")
    public ResultInfo updateData(@RequestParam String tableName, @RequestParam String recordId, @RequestBody Map<String, String> data) {
        String event = Constant.UPDATE_TABLE_DATA + ", recordId is " + recordId;
        try {
            boolean isSuccess = tableService.updateData(tableName, recordId, data);
            return isSuccess ? recordSuccessLogAndReturn(event) : recordFailedLogAndReturn(event);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.UPDATE_TABLE_DATA + "，请求参数：tableName:" + tableName + "," + data.toString(), e);
            // 回显hibernate返回的USQLException的message信息
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @PostMapping("/deleteData")
    public ResultInfo deleteData(@RequestParam String tableName, @RequestParam String recordId) {
        String event = Constant.DELETE_TABLE_DATA + ", tableName is " + tableName + ", recordId is " + recordId;
        try {
            boolean isSuccess = tableService.deleteData(tableName, recordId);
            return isSuccess ? recordSuccessLogAndReturn(event) : recordFailedLogAndReturn(event);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event, e);
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @PostMapping("/batchDeleteData")
    public ResultInfo batchDeleteData(@RequestParam String tableName, @RequestBody List<String> recordIdList) {
        String event = Constant.BATCH_DELETE_TABLE_DATA + ", tableName is " + tableName + ", recordIds is " + recordIdList;
        try {
            // 参数校验
            OperUtils.checkArgs(recordIdList);
            boolean isSuccess = tableService.batchDeleteData(tableName, recordIdList);
            return isSuccess ? recordSuccessLogAndReturn(event) : recordFailedLogAndReturn(event);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event, e);
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    @PostMapping("/batchModifyData")
    public ResultInfo batchModifyData(@RequestParam String tableName, @RequestBody List<String> recordIdList
            , @RequestParam String field, @RequestParam String value) {
        String event = Constant.BATCH_UPDATE_TABLE_DATA + ", tableName is " + tableName + ", recordIds is " + recordIdList
                + ", field is " + field + ", value is " + value;
        try {
            // 参数校验
            OperUtils.checkArgs(recordIdList);
            boolean isSuccess = tableService.batchModifyData(tableName, recordIdList, field, value);
            return isSuccess ? recordSuccessLogAndReturn(event) : recordFailedLogAndReturn(event);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + event, e);
            return recordLogAndReturn(event, OperUtils.parseException(e));
        }
    }

    /**
     * 导出：表结构 + 全量数据，下载 JSON 文件
     */
    @GetMapping("/export")
    public void export(@RequestParam String tableName, HttpServletResponse response) {
        String event = "导出表格，tableName is " + tableName;
        try {
            OperUtils.permissionCheck(Constant.SUPER);

            Subject subject = SecurityUtils.getSubject();
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();

            TableTransferPackageDTO exportPackage = tableService.exportTable(tableName, loginUser.getUserName());
            String json = objectMapper.writeValueAsString(exportPackage);

            String safeTableName = tableName.replaceAll("[\\r\\n]", "");
            String filename = URLEncoder.encode(safeTableName + "_export.json", StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

            OutputStream os = response.getOutputStream();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            os.flush();

            webLogService.success(event);
        } catch (Exception e) {
            logger.error("事件：" + event, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 导入：上传文件，后端完成校验/同名表判断/创建表/导入数据。
     *
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultInfo importTable(@RequestParam("file") MultipartFile file) {
        String event = "导入表格";
        try {
            OperUtils.permissionCheck(Constant.SUPER);

            Subject subject = SecurityUtils.getSubject();
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();

            if (file == null || file.isEmpty()) {
                return ResultInfo.failed("文件不能为空！");
            }

            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            tableService.importTable(jsonContent, loginUser.getUserName());

            webLogService.success(event);
            return ResultInfo.success(Constant.OPER_SUCCESS);
        } catch (Exception e) {
            logger.error("事件：" + event, e);
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

    private ResultInfo recordSuccessLogAndReturn(String event) {
        webLogService.success(event);
        return ResultInfo.success(Constant.OPER_SUCCESS);
    }

    private ResultInfo recordFailedLogAndReturn(String event) {
        webLogService.failed(event);
        return ResultInfo.failed();
    }
}
