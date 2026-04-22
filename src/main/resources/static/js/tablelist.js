/**
 * 表格列表相关操作
 */

function initTableListArgs() {
    // 初始化默认数据
    window.currentPageForTableList = 1;
    window.pageSizeForTableList = 15;
    window.searchFieldsForTableList = "";
    window.searchValuesForTableList = "";
    // 重置分页相关元素的值
    renderPageControlsTableList(1, 1);
    // 每页显示条数
    document.getElementById("pageSizeSelect_tableList").value = 15;
}
initTableListArgs();

// 获取所有表的数据
function loadTablesData() {

    // 数据初始化
    let pageNum = window.currentPageForTableList || 1;
    let pageSize = window.pageSizeForTableList || 15;
    const searchFields = window.searchFieldsForTableList || "";
    const searchValues = window.searchValuesForTableList || "";
    const tableBody = document.getElementById("tableList-table-body");
    listTables(pageNum, pageSize, searchFields, searchValues, tableBody);
}

// 发送请求获取表格的列表
function listTables(pageNum, pageSize, searchFields, searchValues, tableBody) {
    // 获取所有列表的数据
    fetch(`/table/list?packageNum=${pageNum}&packageSize=${pageSize}&orderFields='tableId'&order='asc'&searchFields=${searchFields}&search=${searchValues}`, {
        method: "GET",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {

                // 渲染右侧表格数据
                //const tableBody = document.getElementById("tableList-table-body");
                tableBody.innerHTML = ""; // 清空表格内容

                const content = response.data.content;
                console.log("当前的表格列表:", content);

                // 根据当前登录用户的角色权限判断是否显示如下按钮
                const loginUser = window.loginUser || "";
                const isSuper = loginUser.roles[0].roleName === "super";

                // 获取全选框元素
                const headerRow = document.getElementById("tableList-header-row");
                const selectAll = document.getElementById("select-all-checkbox-table");
                const tableListLi = document.getElementById("tableList");
                const menuDescTableOper = document.getElementById("menuDesc_tableOper");

                // 获取顶部按钮和筛选区域元素
                const topButton = document.getElementById("topButton_tableList");
                const filterArea = document.getElementById("filter-area_tableList");

                if (isSuper && !selectAll) {
                    // 创建新的 <th> 元素
                    const newTh = document.createElement("th");

                    // 添加全选框
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.id = "select-all-checkbox-table";
                    checkbox.setAttribute("data-table-id", "tableManager");
                    newTh.appendChild(checkbox);
                    // 将 <th> 添加到 <tr> 的开头
                    headerRow.insertBefore(newTh, headerRow.firstChild);
                } else {
                    // 获取 headerRow 的第一个子元素
                    var firstTh = headerRow.firstElementChild;
                    // 检查第一个 <th> 是否是全选框
                    if (firstTh && firstTh.querySelector("input[type='checkbox']") && firstTh.id === "select-all-checkbox-table") {
                        firstTh.remove(); // 删除全选框列
                    }
                }

                tableListLi.textContent = isSuper ? "表格管理" : "表格列表";
                // 修改导航信息
                menuDescTableOper.textContent = isSuper ? "表格管理" : "表格列表";

                if (!isSuper) {
                    // 使用 !important 强制应用样式，确保它生效，设置display:none表示隐藏此元素
                    topButton.style.setProperty('display', 'none', 'important');
                    filterArea.style.setProperty('display', 'none', 'important');
                }

                content.forEach((row, index) => {
                    const tr = document.createElement("tr");

                    if (isSuper) {
                        // 如果是管理员，添加复选框列
                        const checkboxTd = document.createElement("td");
                        const checkbox = document.createElement("input");
                        checkbox.type = "checkbox";
                        checkbox.className = `row-checkbox tableManager`;
                        checkbox.setAttribute("data-id", row.tableId);
                        checkboxTd.appendChild(checkbox);
                        tr.appendChild(checkboxTd);
                    }

                    // 计算序号的值：当前页码×每页行数+index，index从0开始
                    const serialNumber = (window.currentPageForTableList - 1) * window.pageSizeForTableList + index + 1;

                    const td = `<td>${serialNumber}</td>
                                <td>${row.tableName}</td>
                                <td>${row.tableDesc}</td>
                                <td>${row.createUser}</td>
                                <td>${row.createTime}</td>`;
                    tr.innerHTML += td;

                    // 添加操作列
                    const actionTd = document.createElement("td");
                    actionTd.style.minWidth = "200px";
                    actionTd.style.maxWidth = "200px"

                    const detaManagerButton = document.createElement("button");
                    detaManagerButton.className = "btn btn-sm me-2 btn-primary";
                    detaManagerButton.textContent = "数据管理";
                    detaManagerButton.onclick = () => tableDataManager(row); // 直接传递对象

                    actionTd.appendChild(detaManagerButton);

                    // 如果是系统管理员，可以操作表格，对表格进行管理
                    if (isSuper) {
                        // 如果是表格管理，增加按钮：列管理、修改、删除。
                        const exportButton = document.createElement("button");
                        exportButton.className = "btn btn-sm me-2 btn-success";
                        exportButton.textContent = "导出";
                        exportButton.onclick = () => exportTable(row);
                        actionTd.appendChild(exportButton);

                        const columnManagerButton = document.createElement("button");
                        columnManagerButton.className = "btn btn-sm me-2 btn-primary";
                        columnManagerButton.textContent = "列管理";
                        columnManagerButton.onclick = () => columnManager(row); // 直接传递对象
                        actionTd.appendChild(columnManagerButton);

                        const modifyTableButton = document.createElement("button");
                        modifyTableButton.className = "btn btn-sm me-2 btn-warning";
                        modifyTableButton.textContent = "修改";
                        modifyTableButton.onclick = () => updateTableDialog(row); // 直接传递对象
                        actionTd.appendChild(modifyTableButton);

                        const deleteTableButton = document.createElement("button");
                        deleteTableButton.className = "btn btn-sm me-2 btn-danger";
                        deleteTableButton.textContent = "删除";
                        deleteTableButton.onclick = () => deleteTableDialog(row.tableId); // 直接传递对象
                        actionTd.appendChild(deleteTableButton);
                    }

                    tr.appendChild(actionTd);
                    // 将当前行添加到表格主体
                    tableBody.appendChild(tr);
                });
                // 返回数据：data{"content":[{...}],"packageNum":"1","packageSize":"15","totalNum":"7"}
                // 计算总页数
                let totalPage = Math.ceil(response.data.totalNum / response.data.packageSize);

                // 设置页码相关数据
                renderPageControlsTableList(totalPage, response.data.packageNum);
                addListenerForTableAllSelector();
            } else if (response.resultCode === 401) {
                showAlert("登录已失效，请重新登录！");
                window.location.href = "/";  // 跳转到登录页面
                return;
            } else if (response.resultCode === 500) {
                showAlert("表格列表查询失败！" + response.message);
            } else {
                showAlert("查询失败！");
            }
        })
        .catch(error => console.error("Error:", error));
}

/**
 * 导出：通过 fetch 获取 blob（token 在 header 中），再触发下载。
 */
function exportTable(row) {
    const tableName = row.tableName;
    fetch(`/table/export?tableName=${encodeURIComponent(tableName)}`, {
        method: "GET"
    })
        .then(res => {
            if (!res.ok) {
                throw new Error("导出失败");
            }
            const disposition = res.headers.get("content-disposition") || "";
            let fileName = `${tableName}.uxdm.json`;
            const match = disposition.match(/filename\*\=UTF-8''([^;]+)/i);
            if (match && match[1]) {
                try {
                    fileName = decodeURIComponent(match[1]);
                } catch (e) {
                    // ignore
                }
            }
            return res.blob().then(blob => ({ blob, fileName }));
        })
        .then(({ blob, fileName }) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        })
        .catch(err => {
            console.error(err);
            showAlert("导出失败，请稍后重试！");
        });
}

/**
 * 打开导入对话框
 */
function openImportDialog() {
    const input = document.getElementById("tableImportFileInput");
    if (!input) {
        showAlert("导入组件初始化失败！");
        return;
    }
    input.value = "";
    input.click();
}

// 绑定导入文件选择事件（页面加载后即可生效）
document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("tableImportFileInput");
    if (!input) return;
    input.addEventListener("change", () => {
        if (!input.files || input.files.length === 0) return;
        const file = input.files[0];
        importTableFile(file);
    });
});

function importTableFile(file) {
    const formData = new FormData();
    formData.append("file", file);
    const ok = window.confirm("确认导入该文件数据吗？");
    if (!ok) {
        return;
    }
    fetch(`/table/import`, {
        method: "POST",
        body: formData
    })
        .then(r => r.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("导入成功！");
                loadTablesData();
            } else if (response.resultCode === 401) {
                showAlert("登录已失效，请重新登录！");
                window.location.href = "/";
                return;
            } else {
                // 500：已存在同名表；400：文件格式错误
                showAlert(response.message || "导入失败！");
            }
        })
        .catch(err => {
            console.error(err);
            showAlert("导入失败：请检查文件格式或稍后重试！");
        });
}

function addListenerForTableAllSelector() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox-table');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            const tableId = selectAllCheckbox.getAttribute('data-table-id');
            const rowCheckboxes = document.querySelectorAll(`.${tableId}.row-checkbox`);

            // 根据全选框状态联动所有行复选框
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }
}

// 搜索按钮的点击事件
function applyFilterForTable() {
    let tableName = document.getElementById("filterTableNameInput").value;
    let tableDesc = document.getElementById("filterTableDescInput").value;
    let createUser = document.getElementById("filterCreateUserInput").value;
    let startTime = document.getElementById("filterStartTimeInput").value;
    let endTime = document.getElementById("filterEndTimeInput").value;

    let searchFields = "tableName,tableDesc,createUser,startTime,endTime";
    let searchValues = `${tableName},${tableDesc},${createUser},${startTime},${endTime}`;

    window.searchFieldsForTableList = searchFields;
    window.searchValuesForTableList = searchValues;

    loadTablesData();
}

// 数据管理按钮的点击事件，点击后显示当前表的数据管理div
function tableDataManager(row) {
    // 隐藏表格列表的操作区域
    document.getElementById("tableListDiv").classList.remove("active");
    // 显示具体表格的操作区域
    document.getElementById("tableOperDiv").classList.add("active");
    // 设置选中的表格的数据
    window.dataList = row;
    console.log("当前表格数据:", window.dataList);
    
    // 清空筛选条件区域
    const filterArea = document.getElementById("filter-area");
    filterArea.innerHTML = '';
    // 更新过滤器下拉框的可选内容
    initFilter("");

    // 更新页面上的表描述提示信息
    const tableDescSpan = document.getElementById("tableDesc_tableOper");
    tableDescSpan.textContent = row.tableDesc;

    // 加载数据
    initTableDataArgs();
    loadTableData();
}

function changeTableListPage() {
    const selectedPage = document.getElementById("currentPageSelect_tableList").value;
    const currentPage = parseInt(selectedPage);
    window.currentPageForTableList = currentPage;
    loadTablesData();
}

// 改变每页显示条数
function changeTableListPageSize() {
    const selectedSize = document.getElementById("pageSizeSelect_tableList").value;
    const pageSize = parseInt(selectedSize);
    window.pageSizeForTableList = pageSize;
    window.currentPageForTableList = 1;  // 切换每页条数时，回到第一页
    loadTablesData();
}

// 渲染分页控件
function renderPageControlsTableList(totalPages, currentPage) {
    const pageSelect = document.getElementById("currentPageSelect_tableList");
    pageSelect.innerHTML = ""; // 清空旧选项

    // 填充页码下拉框
    for (let i = 1; i <= totalPages; i++) {
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        if (i === currentPage) option.selected = true;
        pageSelect.appendChild(option);
    }

    // 更新总页数信息
    document.getElementById("totalPageInfo_tableList").textContent = `总页数: ${totalPages}`;
}

/********************************* 新增表格 *********************************/

function showAddTableModal() {
    // 每次打开模态框时，清空所有输入框的值
    const form = document.getElementById("addTableForm");
    resetForm(form);

    const container = document.getElementById("columns-container");
    container.innerHTML = "";

    // 显示弹框
    const addModal = new bootstrap.Modal(document.getElementById("addTableModal"));
    addModal.show();
}

function addColumn() {
    const container = document.getElementById("columns-container");

    const row = document.createElement("div");
    row.classList.add("row", "mb-2", "align-items-center");

    row.innerHTML = `
                <div class="col-auto">
                    <button class="btn btn-danger btn-sm" type="button">删除</button>
                </div>
                <div class="col">
                    <input type="text" class="form-control column-name" placeholder="列名">
                </div>
                <div class="col">
                    <input type="text" class="form-control column-description" placeholder="列描述">
                </div>
                <div class="col">
                    <input type="text" class="form-control column-display-multiplier" placeholder="列宽倍数" value="1">
                </div>
                <div class="col">
                    <select class="form-select column-type">
                        <option value="input" selected>输入框</option>
                        <option value="select">下拉框</option>
                    </select>
                </div>
                <div class="col">
                    <input type="text" class="form-control column-options" placeholder="下拉框可选项">
                </div>
            `;

    row.querySelector(".btn-danger").addEventListener("click", function () {
        if (showAlert("确认删除此列？")) {
            row.remove();
        }
    });

    container.appendChild(row);
}

function isValidForAddTable(tableName, tableDesc) {
    // 表格名称不能为空，表格描述不能为空
    // 校验数据
    if (tableName === "") {
        showAlert("表格名称不能为空！");
        return;
    }
    if (tableDesc === "") {
        showAlert("表格描述不能为空！");
        return;
    }
    return true;
}

function isValidForAddTableColumn(columnName, columnDesc, displayMultiplier, controlType, enumValues) {
    // 不能创建字段名称为id的字段
    if (columnName === "id") {
        showAlert("id列为默认主键，不能手动创建！");
        return;
    }

    if (columnName === "") {
        showAlert("列名不能为空！");
        return;
    }

    if (columnDesc === "") {
        showAlert("列描述不能为空！");
        return;
    }

    if (displayMultiplier === "") {
        showAlert("列宽倍数不能为空！");
        return;
    }

    if (controlType === "select" && enumValues === "") {
        showAlert("组件类型为下拉框时，下拉框可选项不能为空！");
        return;
    }

    return true;
}

function saveTable() {

    // 封装表单数据
    const tableName = document.getElementById("tableNameAdd").value;
    const tableDesc = document.getElementById("tableDescAdd").value;

    if (!isValidForAddTable(tableName, tableDesc)) {
        return;
    }

    // 提取列信息

    // 获取所有 column-name 和 column-desc 输入框
    const columnNames = document.querySelectorAll(".column-name");
    const columnDescList = document.querySelectorAll(".column-description");
    const columnDisplayMultipliers = document.querySelectorAll(".column-display-multiplier");
    const columnTypes = document.querySelectorAll(".column-type");
    const columnOptionsList = document.querySelectorAll(".column-options");

    // 构造 JSON 数据
    const columns = [];
    const columnNamesSet = new Set();
    const columnDescsSet = new Set();
    for (let i = 0; i < columnNames.length; i++) {

        const columnName = columnNames[i].value;
        const columnDesc = columnDescList[i].value;

        // 校验：不能存在重复的id或描述
        if (columnNamesSet.has(columnName)) {
            showAlert(`错误：存在重复的列名"${columnName}"！`);
            return;
        }
        if (columnDescsSet.has(columnDesc)) {
            showAlert(`错误：存在重复的列描述"${columnDesc}"！`);
            return;
        }

        if (!isValidForAddTableColumn(columnName, columnDesc, columnDisplayMultipliers[i].value, columnTypes[i].value, columnOptionsList[i].value)) {
            return;
        }

        columns.push({
            columnName: columnNames[i].value,
            columnDesc: columnDescList[i].value,
            displayMultiplier: columnDisplayMultipliers[i].value,
            controlType: columnTypes[i].value,
            enumValues: columnOptionsList[i].value
        });

        columnNamesSet.add(columnName);
        columnDescsSet.add(columnDesc);
    }

    const requestData = {
        tableName,
        tableDesc,
        columns,
    };

    // 发起保存请求
    fetch(`/table/save`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("保存成功！");
                console.log("保存成功:", response);

                closeModal("addTableModal");
                // 刷新表格
                loadTablesData();

            } else if (response.resultCode === 500) {
                // 500属于后端返回的报错，此请求有部分参数校验需要在后端完成，且页面参数信息较多，所以返回500错误码时，显示提示信息且不关闭新增表格弹框
                showAlert("保存失败！" + response.message);
            } else {
                closeModal("addTableModal");
                responseParse(response, response.message, "保存失败！");
            }
        })
        .catch(error => console.error("保存失败:", error));
}

/********************************* 修改表格 *********************************/

function updateTableDialog(row) {
    // 回显数据
    // 每次打开模态框时，回显输入框的值
    document.getElementById("tableNameModify").value = row.tableName;
    document.getElementById("tableNameModify").disabled = true;
    document.getElementById("tableDescModify").value = row.tableDesc;

    // 显示弹框
    const addModal = new bootstrap.Modal(document.getElementById("modifyTableModal"));
    document.getElementById("modifyTableButton").onclick = () => updateTable(row.tableId);
    addModal.show();
}

function updateTable(tableId) {
    // 获取表单数据
    const tableName = document.getElementById("tableNameModify").value;
    const tableDesc = document.getElementById("tableDescModify").value;

    const tableInfo = {
        tableId: tableId,
        tableName: tableName,
        tableDesc: tableDesc
    }
    // 发送请求修改表格信息
    // 发起保存请求
    fetch(`/table/update`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(tableInfo)
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("修改成功！");
                console.log("修改成功:", response);

                // 刷新表格
                loadTablesData();
            } else if (response.resultCode === 500) {
                showAlert("修改失败！" + response.message);
            } else {
                showAlert("修改失败！");
            }
        })
        .catch(error => console.error("修改失败:", error))
        .finally(() => {
            // 关闭弹框
            const addModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("modifyTableModal"));
            addModal.hide();
        });
}

/********************************* 删除表格 *********************************/

function deleteTableDialog(id) {
    const deleteModal = new bootstrap.Modal(document.getElementById("deleteModal"));
    document.getElementById("confirmDeleteButton").onclick = () => deleteTable(id);
    deleteModal.show();
}

function deleteTable(tableId) {
    if (tableId !== null) {
        // 发送请求删除这条记录
        fetch(`/table/delete?tableId=${tableId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 200) {
                    showAlert("删除成功！");
                    console.log("删除成功:", response);
                    // 刷新表格
                    loadTablesData();
                } else if (response.resultCode === 500) {
                    showAlert("删除失败！" + response.message);
                } else {
                    showAlert("删除失败！");
                }
            })
            .catch(error => console.error("删除失败:", error))
            .finally(() => {
                // 关闭模态框
                const deleteModal = bootstrap.Modal.getInstance(document.getElementById("deleteModal"));
                deleteModal.hide();
            });
    }
}

/********************************* 批量删除表格 *********************************/

let selectedIdsForTable = [];

// 批量删除
function showBatchDeleteTableModal() {
    // 获取选中行的 ID
    selectedIdsForTable = Array.from(document.querySelectorAll(".row-checkbox:checked"))
        .map(checkbox => checkbox.getAttribute("data-id"));
    if (selectedIdsForTable.length === 0) {
        showAlert("请先选择要删除的记录！");
        return;
    }

    // 修改确认按钮的onclick
    // 添加点击事件
    const batchDeleteButton = document.getElementById("confirmBatchDelete");
    batchDeleteButton.onclick = () => batchDeleteTable();

    // 显示模态框
    const deleteModal = new bootstrap.Modal(document.getElementById("batchDeleteModal"));
    deleteModal.show();
}

// 批量删除确认按钮点击事件
function batchDeleteTable() {
    if (selectedIdsForTable.length > 0) {
        console.log("删除的 ID 列表：", selectedIdsForTable);

        // 发送请求删除这条记录
        fetch(`/table/batchDelete`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selectedIdsForTable)
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 200) {
                    showAlert("批量删除成功！");
                    // 刷新表格
                    loadTablesData();
                } else if (response.resultCode === 500) {
                    showAlert("批量删除失败！" + response.message);
                } else {
                    showAlert("批量删除失败！");
                }
            })
            .catch(error => console.error("批量删除失败：", error))
            .finally(() => {
                // 关闭模态框
                const deleteModal = bootstrap.Modal.getInstance(document.getElementById("batchDeleteModal"));
                deleteModal.hide();
                selectedIdsForUser = [];
            });
    }
}

/********************************* 列管理 *********************************/

function columnManager(row) {
    // 跳转到列管理界面
    // 隐藏表格列表的操作区域
    document.getElementById("tableListDiv").classList.remove("active");
    // 显示列管理的操作区域
    document.getElementById("columnManagerDiv").classList.add("active");
    // 设置选中的表格的数据
    window.dataList = row;
    console.log("当前表格数据:", window.dataList);

    // 更新页面上的表描述提示信息
    const tableDescSpan = document.getElementById("tableDesc_tableList");
    tableDescSpan.textContent = row.tableDesc;

    // 加载数据
    initColumnArgs();
    listColumns();
}