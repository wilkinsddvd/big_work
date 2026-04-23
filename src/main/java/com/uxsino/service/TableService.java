package com.uxsino.service;

import com.uxsino.DTO.AddColumnDTO;
import com.uxsino.DTO.AddTableDTO;
import com.uxsino.DTO.PageListDTO;
import com.uxsino.DTO.TableTransferPackageDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.response.ResultPage;
import com.uxsino.common.utils.LogConsts;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.entity.TableColumnDao;
import com.uxsino.entity.TableDao;
import com.uxsino.repository.TableColumnRepository;
import com.uxsino.repository.TableRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 17:15
 */
@Service
@Transactional
public class TableService {

    private static final Logger logger = LoggerFactory.getLogger(TableService.class);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TableColumnRepository tableColumnRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    public Page<TableDao> listTable(PageListDTO tableDTO) {

        logger.debug(Constant.LIST_TABLE_MANAGER + "，请求参数为：" + tableDTO.toString());

        // 封装搜索字段
        PageRequest pageRequest = OperUtils.getPageRequest(tableDTO, Constant.TABLE_MANAGER_ORDER_FIELDS);
        String[] searchFieldsArray = OperUtils.getSearchFieldsArray(tableDTO.getSearchFields());
        String[] searchArray = OperUtils.getSearchArray(tableDTO.getSearch());

        Page<TableDao> page = tableRepository.findAll(new Specification<TableDao>() {
            @Override
            public Predicate toPredicate(Root<TableDao> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();

                // 如果筛选条件为空或长度不匹配，返回空查询
                if (searchFieldsArray == null || searchArray == null
                        || searchArray.length != searchFieldsArray.length) {
                    return query.getRestriction();
                }
                Date startDate = null;
                Date endDate = null;
                for (int i = 0; i < searchFieldsArray.length; i++) {
                    String key = searchFieldsArray[i];
                    if (Constant.TABLE_NAME.equals(key) || Constant.TABLE_DESC.equals(key) || Constant.CREATE_USER.equals(key)) {
                        // 模糊匹配
                        predicates.add(builder.like(root.get(key), LogConsts.WILDCARD + searchArray[i] + LogConsts.WILDCARD));
                    } else if (Constant.START_TIME.equals(key)) {
                        // 精准匹配
                        try {
                            startDate = dateFormat.parse(searchArray[i].replace("T", " ") + LogConsts.DATE_COMP);
                        } catch (ParseException e) {
                            // ignore
                        }
                    } else if (Constant.END_TIME.equals(key)) {
                        try {
                            endDate = dateFormat.parse(searchArray[i].replace("T", " ") + LogConsts.DATE_COMP);
                        } catch (ParseException e) {
                            // ignore
                        }
                    }
                }
                // 时间校验：有一个时间不为空时，需要增加时间过滤
                if (startDate != null || endDate != null) {
                    if (startDate == null) {
                        // 构建时间范围条件：小于或等于结束时间
                        predicates.add(builder.lessThanOrEqualTo(root.<Date>get(Constant.CREATE_TIME), endDate));
                    } else if (endDate == null) {
                        // 查询某个时间之后的所有记录
                        predicates.add(builder.greaterThanOrEqualTo(root.<Date>get(Constant.CREATE_TIME), startDate));
                    } else {
                        predicates.add(builder.between(root.<Date> get(Constant.CREATE_TIME), startDate, endDate));
                    }
                }

                // 动态加载关联关系
                root.fetch("tableColumnList", JoinType.LEFT);
                // 去重
                query.distinct(true);
                Predicate predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                return query.where(predicate).getRestriction();
            }
        }, pageRequest);

        return page;
    }

    /**
     * 创建表格
     * <p>
     *     1. 在数据库中创建对应名称和字段的数据库表
     *     2. 将表格数据保存在tbl_tables表中
     *     3. 将列数据保存在tbl_columns表中
     * </p>
     * @param addTableDTO
     * @param loginUserName
     * @return
     * @Transactional 默认只会在未捕获的运行时异常（RuntimeException 或其子类，如 NullPointerException、IllegalArgumentException）或 Error 时回滚。而对于检查型异常（CheckedException），例如 IOException 或 SQLException，默认不会触发事务回滚。rollbackFor = {Exception.class}表示发生了任何异常都回滚
     */
    @Transactional(rollbackFor = {Exception.class})
    public boolean save(AddTableDTO addTableDTO, String loginUserName){
        // 校验表是否存在：表名和表描述均不能重名
        List<TableDao> tableDaoList = tableRepository.findByTableName(addTableDTO.getTableName());
        if (tableDaoList != null && tableDaoList.size() > 0) {
            throw new RuntimeException("相同名称的表格已存在！");
        }
        tableDaoList = tableRepository.findByTableDesc(addTableDTO.getTableDesc());
        if (tableDaoList != null && tableDaoList.size() > 0) {
            throw new RuntimeException("相同描述的表格已存在！");
        }

        // 动态构造 SQL
        /**
         * CREATE TABLE tbl_work_order (
         *     id SERIAL PRIMARY KEY,
         *     name character varying NULL
         *     );
         */
        StringBuilder sql = new StringBuilder("CREATE TABLE \"").append(addTableDTO.getTableName()).append("\" ( ")
                .append("id SERIAL PRIMARY KEY");

        List<AddColumnDTO> columnDTOList = addTableDTO.getColumns();

        if (columnDTOList != null) {
            if (columnDTOList.size() > 0) {
                sql.append(",");
            }
            for (int i = 0; i < columnDTOList.size(); i++) {
                AddColumnDTO addColumnDTO = columnDTOList.get(i);

                sql.append("\"").append(addColumnDTO.getColumnName()).append("\"").append(" character varying NULL");
                if (i < columnDTOList.size() - 1) {
                    sql.append(",");
                }
            }
        }
        sql.append(" );");
        // 在数据库中创建对应名称的表格
        entityManager.createNativeQuery(sql.toString()).executeUpdate();

        // 将在数据库中创建对应表格的名称操作放在最前面执行，让数据库校验此表格名称是否符合规则
        // 如果创建成功，继续保存其他数据

        // 将DTO拆分为表数据和列数据，分别存储在对应数据库表中
        TableDao tableDao = TableDao.builder().tableName(addTableDTO.getTableName())
                .tableDesc(addTableDTO.getTableDesc())
                .createUser(loginUserName)
                .createTime(new Date()).build();
        // 保存tableDao并获取保存后的Dao，后续保存列数据时需要使用其tableId
        TableDao saveDao = tableRepository.save(tableDao);

        // 保存一个名为id的列信息
        TableColumnDao tableColumnId = TableColumnDao.builder().tableId(saveDao.getTableId())
                .columnName("id").columnDesc("编号").controlType("input").displayMultiplier("1")
                .table(tableDao).build();
        tableColumnRepository.save(tableColumnId);
        for (int i = 0; i < columnDTOList.size(); i++) {
            AddColumnDTO addColumnDTO = columnDTOList.get(i);
            TableColumnDao tableColumnDao = TableColumnDao.builder().tableId(saveDao.getTableId())
                    .columnName(addColumnDTO.getColumnName())
                    .columnDesc(addColumnDTO.getColumnDesc())
                    .controlType(addColumnDTO.getControlType())
                    .displayMultiplier(addColumnDTO.getDisplayMultiplier())
                    .table(tableDao).build();
            if ("select".equals(addColumnDTO.getControlType()) && addColumnDTO.getEnumValues() != null) {
                tableColumnDao.setEnumValues(addColumnDTO.getEnumValues());
            }
            tableColumnRepository.save(tableColumnDao);
        }
        return true;
    }

    public ResultPage<Map<String, Object>> getTableData(PageListDTO tableDTO) {

        logger.debug(Constant.LIST_TABLE_DATA + "，请求参数为：" + tableDTO.toString());

        String tableName = tableDTO.getTableName();
        // 获取表结构（字段信息）
        List<String> columnNames = getTableMetadata(tableName);

        // 动态构造 SQL 查询
        StringBuilder sql = new StringBuilder("SELECT * FROM \"").append(tableName).append("\" ");

        String[] searchFieldsArray = OperUtils.getSearchFieldsArray(tableDTO.getSearchFields());
        String[] searchArray = OperUtils.getSearchArray(tableDTO.getSearch());

        // 动态拼接 WHERE 条件
        if (searchFieldsArray != null && searchArray != null && searchFieldsArray.length > 0 && searchFieldsArray.length == searchArray.length) {
            sql.append(" WHERE ");
            for (int i = 0; i < searchFieldsArray.length; i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("\"").append(searchFieldsArray[i]).append("\"").append(" LIKE :searchValue").append(i);
            }
        }

        // 动态拼接排序
        String sortField = tableDTO.getOrderFields();
        if (sortField != null && !sortField.isEmpty()) {
            sql.append(" ORDER BY ").append(sortField).append(" ").append(tableDTO.getOrder() != null ? tableDTO.getOrder() : "ASC");
        }

        // 分页
        sql.append(" LIMIT :pageSize OFFSET :offset");

        Query query = entityManager.createNativeQuery(sql.toString());

        // 设置模糊匹配参数
        if (searchFieldsArray != null && searchArray != null) {
            for (int i = 0; i < searchFieldsArray.length; i++) {
                query.setParameter("searchValue" + i, "%" + searchArray[i] + "%");
            }
        }

        // 设置分页参数
        query.setParameter("pageSize", tableDTO.getPackageSize());
        query.setParameter("offset", (tableDTO.getPackageNum() - 1) * tableDTO.getPackageSize());

        // 执行查询并获取数据
        List<Object[]> results = query.getResultList();

        // 将结果映射为键值对格式
        List<Map<String, Object>> tableData = new ArrayList<>();

        // 如果仅有一个id列，则results会被映射为对象列表List<Object>，如下foreach循环会报错，需要增加判断(此场景在添加数据后，删除其余列仅剩id列时出现)
        if (results != null && results.size() > 0 && !(results.get(0) instanceof Object[])) {
            for (Object row : results) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    rowData.put(columnNames.get(i), row);
                }
                tableData.add(rowData);
            }
        } else {
            for (Object[] row : results) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    rowData.put(columnNames.get(i), row[i]);
                }
                tableData.add(rowData);
            }
        }

        // 获取总记录数：再次执行不添加分页的count(*)语句
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM \"").append(tableName).append("\" ");
        if (searchFieldsArray != null && searchArray != null && searchFieldsArray.length > 0) {
            countSql.append(" WHERE ");
            for (int i = 0; i < searchFieldsArray.length; i++) {
                if (i > 0) {
                    countSql.append(" AND ");
                }
                countSql.append("\"").append(searchFieldsArray[i]).append("\"").append(" LIKE :searchValue").append(i);
            }
        }

        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        if (searchFieldsArray != null && searchArray != null) {
            for (int i = 0; i < searchFieldsArray.length; i++) {
                countQuery.setParameter("searchValue" + i, "%" + searchArray[i] + "%");
            }
        }
        long totalRecords = ((Number) countQuery.getSingleResult()).longValue();

        ResultPage<Map<String, Object>> resultPage = ResultPage.restPage(tableDTO.getPackageNum(), tableDTO.getPackageSize(), totalRecords, tableData);

        return resultPage;
    }

    public boolean saveData(String tableName, Map<String, String> data) {
        List<String> columnNames = getTableMetadata(tableName);
        // 动态构造 SQL
        StringBuilder sql = new StringBuilder("INSERT INTO \"").append(tableName).append("\" ");
        StringBuilder fileds = new StringBuilder("");
        StringBuilder values = new StringBuilder("");
        int index = 1;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            // 不拼接序号，序号自动生成
            fileds.append("\"").append(columnNames.get(index)).append("\"");
            values.append("'").append(entry.getValue()).append("'");
            if (index++ < data.size()) {
                fileds.append(",");
                values.append(",");
            }
        }
        sql = sql.append("(").append(fileds).append(")").append(" VALUES ").append("(").append(values).append(")");
        int result = entityManager.createNativeQuery(sql.toString()).executeUpdate();
        return result > 0;
    }

    public boolean updateData(String tableName, String recordId, Map<String, String> data) {
        // 根据id获取当前数据
        Map<String, Object> tableData = getDataById(tableName, recordId);

        StringBuilder sql = new StringBuilder("UPDATE \"").append(tableName).append("\" SET ");

        Iterator<String> iterator = tableData.keySet().iterator();

        boolean hasUpdate = false;
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object tableDataObj = tableData.get(key);
            if (data.containsKey(key) && !data.get(key).equals(tableDataObj == null ? "" : tableDataObj.toString())) {
                // 从第二个需要更新的数据开始，每个更新的数据前面添加逗号
                if (hasUpdate) {
                    sql.append(",");
                }
                sql.append("\"").append(key).append("\"").append("='").append(data.get(key)).append("'");
                hasUpdate = true;
            }
        }

        sql.append(" WHERE id=:recordId");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("recordId", recordId);
        int result = query.executeUpdate();
        return result > 0;
    }

    public Map<String, Object> getDataById(String tableName, String recordId) {
        List<String> columnNames = getTableMetadata(tableName);
        // 动态构造 SQL 查询
        StringBuilder sql = new StringBuilder("SELECT * FROM \"").append(tableName).append("\" WHERE id = :recordId");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("recordId", recordId);
        List<Object[]> results = query.getResultList();
        // 将结果映射为键值对格式
        Map<String, Object> rowData = new HashMap<>();
        if (results.size() == 1) {
            for (int i = 0; i < columnNames.size(); i++) {
                rowData.put(columnNames.get(i), results.get(0)[i]);
            }
        }
        return rowData;
    }

    public List<String> getTableMetadata(String tableName) {
        // 获取表结构（字段信息）
        List<String> columnNames = new ArrayList<>();
        // 从 EntityManager 获取 Hibernate Session
        Session session = entityManager.unwrap(Session.class);

        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                DatabaseMetaData metaData = connection.getMetaData();
                logger.debug("获取表元数据，表名：{}", tableName);

                // PostgreSQL 将未加引号的标识符存储为小写，getColumns 需要精确匹配大小写。
                // 依次尝试原始名称、小写、大写，以兼容不同数据库和命名风格。
                String resolvedName = resolveTableName(metaData, tableName);
                if (resolvedName == null) {
                    logger.warn("未能通过 DatabaseMetaData 获取表 {} 的列信息", tableName);
                    return;
                }

                ResultSet columns = metaData.getColumns(null, null, resolvedName, null);
                while (columns.next()) {
                    columnNames.add(columns.getString("COLUMN_NAME"));
                }
                columns.close();
                logger.debug("表 {} 共获取到 {} 个列", resolvedName, columnNames.size());
            }
        });

        if (columnNames.isEmpty()) {
            throw new RuntimeException("无法获取表 [" + tableName + "] 的列信息，请确认表名是否正确");
        }
        return columnNames;
    }

    /**
     * 尝试以原始名称、小写、大写顺序在数据库元数据中查找表名，返回匹配的实际名称；未找到返回 null。
     */
    private String resolveTableName(DatabaseMetaData metaData, String tableName) throws SQLException {
        for (String candidate : new String[]{tableName, tableName.toLowerCase(), tableName.toUpperCase()}) {
            ResultSet rs = metaData.getColumns(null, null, candidate, null);
            boolean found = rs.next();
            rs.close();
            if (found) {
                return candidate;
            }
        }
        return null;
    }

    public boolean deleteData(String tableName, String recordId) {
        // 拼接语句
        // 动态构造 SQL 查询
        StringBuilder sql = new StringBuilder("DELETE FROM \"").append(tableName).append("\" WHERE id = :recordId");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("recordId", recordId);
        int result = query.executeUpdate();
        return result > 0;
    }

    public boolean batchDeleteData(String tableName, List<String> recordIdList) {
        // 拼接语句
        // 动态构造 SQL 查询
        StringBuilder sql = new StringBuilder("DELETE FROM \"").append(tableName).append("\" WHERE ");
        for (int i = 0; i < recordIdList.size(); i++) {
            sql.append("id=:recordId").append(i);
            if (i < recordIdList.size() - 1) {
                sql.append(" OR ");
            }
        }
        Query query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < recordIdList.size(); i++) {
            query.setParameter("recordId" + i, recordIdList.get(i));
        }
        int result = query.executeUpdate();
        return result > 0;
    }

    public boolean batchModifyData(String tableName, List<String> recordIdList, String field, String value) {
        StringBuilder sql = new StringBuilder("UPDATE \"").append(tableName).append("\" SET ");
        sql.append(field).append("=:fieldValue WHERE ");
        for (int i = 0; i < recordIdList.size(); i++) {
            sql.append("id=:recordId").append(i);
            if (i < recordIdList.size() - 1) {
                sql.append(" OR ");
            }
        }
        Query query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < recordIdList.size(); i++) {
            query.setParameter("recordId" + i, recordIdList.get(i));
        }
        query.setParameter("fieldValue", value);
        int result = query.executeUpdate();
        return result > 0;
    }

    public void delete(TableDao tableDao) {
        // 删除对应列信息
        List<TableColumnDao> columnList = tableDao.getTableColumnList();
        for (TableColumnDao tableColumnDao : columnList) {
            Optional<TableColumnDao> columnDao = tableColumnRepository.findById(Long.parseLong(tableColumnDao.getColumnId().toString()));
            // 如果获取到值
            // 删除
            columnDao.ifPresent(dao -> tableColumnRepository.delete(dao));
        }

        // 删除表格信息
        tableRepository.delete(tableDao);

        // 删除对应实际数据库表
        // 动态构造 SQL
        /**
         * DROP TABLE tbl_work_order;
         */
        entityManager.createNativeQuery("DROP TABLE \"" + tableDao.getTableName() + "\"").executeUpdate();
    }

    public TableDao findByTableId(Long tableId) {
        return tableRepository.findByTableId(tableId);
    }

    public boolean save(TableDao tableDao){
        return null != tableRepository.save(tableDao);
    }

    /**
     * 获取表的全量数据（不分页）
     */
    public List<Map<String, Object>> getTableDataFull(String tableName) {
        List<String> columnNames = getTableMetadata(tableName);
        StringBuilder sql = new StringBuilder("SELECT * FROM \"").append(tableName).append("\"");
        Query query = entityManager.createNativeQuery(sql.toString());
        List results = query.getResultList();

        List<Map<String, Object>> tableData = new ArrayList<>();
        if (results != null && results.size() > 0 && !(results.get(0) instanceof Object[])) {
            for (Object row : results) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    rowData.put(columnNames.get(i), row);
                }
                tableData.add(rowData);
            }
        } else if (results != null) {
            for (Object[] row : (List<Object[]>) results) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    rowData.put(columnNames.get(i), row[i]);
                }
                tableData.add(rowData);
            }
        }
        return tableData;
    }

    /**
     * 导出表结构和全量数据，返回导出包对象
     */
    public TableTransferPackageDTO exportTable(String tableName, String loginUserName) {
        List<TableDao> tableDaoList = tableRepository.findByTableName(tableName);
        if (tableDaoList == null || tableDaoList.isEmpty()) {
            throw new RuntimeException("表格不存在：" + tableName);
        }
        TableDao tableDao = tableDaoList.get(0);

        TableTransferPackageDTO dto = new TableTransferPackageDTO();
        dto.setFormat("big_work_table_export_v1");
        dto.setVersion(1);

        TableTransferPackageDTO.Meta meta = new TableTransferPackageDTO.Meta();
        meta.setExportedAt(dateFormat.format(new Date()));
        meta.setExportedBy(loginUserName);
        TableTransferPackageDTO.Source source = new TableTransferPackageDTO.Source();
        source.setApp("big_work");
        source.setDb("postgresql");
        meta.setSource(source);
        dto.setMeta(meta);

        TableTransferPackageDTO.TableInfo tableInfo = new TableTransferPackageDTO.TableInfo();
        tableInfo.setTableName(tableDao.getTableName());
        tableInfo.setTableDesc(tableDao.getTableDesc());
        dto.setTable(tableInfo);

        List<TableTransferPackageDTO.Column> columns = new ArrayList<>();
        for (TableColumnDao col : tableDao.getTableColumnList()) {
            TableTransferPackageDTO.Column column = new TableTransferPackageDTO.Column();
            column.setColumnName(col.getColumnName());
            column.setColumnDesc(col.getColumnDesc());
            column.setDisplayMultiplier(col.getDisplayMultiplier());
            column.setControlType(col.getControlType());
            column.setEnumValues(col.getEnumValues());
            columns.add(column);
        }
        dto.setColumns(columns);

        dto.setData(getTableDataFull(tableDao.getTableName()));
        return dto;
    }

    /**
     * 导入表结构和全量数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void importTable(String jsonContent, String loginUserName) throws Exception {
        TableTransferPackageDTO dto = objectMapper.readValue(jsonContent, TableTransferPackageDTO.class);

        if (!"big_work_table_export_v1".equals(dto.getFormat())) {
            throw new RuntimeException("文件格式错误：不是有效的导出文件！");
        }
        if (dto.getTable() == null || dto.getColumns() == null) {
            throw new RuntimeException("文件内容不完整！");
        }

        String tableName = dto.getTable().getTableName();
        List<TableDao> existingTables = tableRepository.findByTableName(tableName);
        if (existingTables != null && !existingTables.isEmpty()) {
            throw new RuntimeException("已存在同名表：" + tableName);
        }

        AddTableDTO addTableDTO = new AddTableDTO();
        addTableDTO.setTableName(dto.getTable().getTableName());
        addTableDTO.setTableDesc(dto.getTable().getTableDesc());

        List<AddColumnDTO> columnDTOs = new ArrayList<>();
        for (TableTransferPackageDTO.Column col : dto.getColumns()) {
            if ("id".equals(col.getColumnName())) {
                continue;
            }
            AddColumnDTO addColumnDTO = new AddColumnDTO();
            addColumnDTO.setColumnName(col.getColumnName());
            addColumnDTO.setColumnDesc(col.getColumnDesc());
            addColumnDTO.setDisplayMultiplier(col.getDisplayMultiplier());
            addColumnDTO.setControlType(col.getControlType());
            addColumnDTO.setEnumValues(col.getEnumValues());
            columnDTOs.add(addColumnDTO);
        }
        addTableDTO.setColumns(columnDTOs);

        save(addTableDTO, loginUserName);

        if (dto.getData() != null && !dto.getData().isEmpty()) {
            // Retrieve column names from DB metadata to avoid using tainted user-provided column names in SQL
            List<String> dataColumns = getTableMetadata(tableName);
            // Remove the id column so we don't try to insert into the auto-generated primary key
            dataColumns.remove("id");

            if (!dataColumns.isEmpty()) {
                for (Map<String, Object> row : dto.getData()) {
                    StringBuilder sql = new StringBuilder("INSERT INTO \"").append(tableName).append("\" (");
                    StringBuilder params = new StringBuilder("VALUES (");
                    for (int i = 0; i < dataColumns.size(); i++) {
                        if (i > 0) {
                            sql.append(",");
                            params.append(",");
                        }
                        sql.append("\"").append(dataColumns.get(i)).append("\"");
                        params.append(":v").append(i);
                    }
                    sql.append(") ").append(params).append(")");
                    Query q = entityManager.createNativeQuery(sql.toString());
                    for (int i = 0; i < dataColumns.size(); i++) {
                        Object value = row.get(dataColumns.get(i));
                        q.setParameter("v" + i, value == null ? null : value.toString());
                    }
                    q.executeUpdate();
                }
            }
        }
    }
}
