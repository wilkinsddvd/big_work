/**
 * 列管理相关操作
 */

/********************************* 获取当前表格的所有列数据 *********************************/

function initColumnArgs() {
    // 初始化默认数据
    window.currentPageForColumn = 1;
    window.pageSizeForColumn = 15;
    window.searchFieldsForColumn = "";
    window.searchValuesForColumn = "";
    // 重置分页相关元素的值
    renderPageControlsColumnManager(1, 1);
    // 每页显示条数
    document.getElementById("pageSizeSelectForColumn").value = 15;
}
initColumnArgs();

// 获取选中表格的列的数据
function listColumns() {

    // 数据初始化
    let pageNum = window.currentPageForColumn || 1;
    let pageSize = window.pageSizeForColumn || 15;
    const searchFields = window.searchFieldsForColumn || "";
    const searchValues = window.searchValuesForColumn || "";

    const tableId = window.dataList.tableId;

    // 获取所有列表的数据
    fetch(`/column/list?packageNum=${pageNum}&packageSize=${pageSize}&orderFields='columnId'&order='asc'&searchFields=${searchFields}&search=${searchValues}&tableId=${tableId}`, {
        method: "GET",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {

                // 渲染右侧表格数据
                const tableBody = document.getElementById("columnManager-table-body");
                tableBody.innerHTML = ""; // 清空表格内容

                const content = response.data.content;
                console.log("当前表格的字段列表:", content);

                content.forEach((row, index) => {
                    const tr = document.createElement("tr");

                    // 添加复选框列
                    const checkboxTd = document.createElement("td");
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.className = `row-checkbox columnManager`;
                    checkbox.setAttribute("data-id", row.columnId);

                    // 如果是id字段，不显示复选框，但需要有td占位
                    if (row.columnName !== "id") {
                        checkboxTd.appendChild(checkbox);
                    }

                    tr.appendChild(checkboxTd);

                    const enumValues = row.enumValues || "";

                    const controlName = getControlNameForControlType(row.controlType);

                    // 计算序号的值：当前页码×每页行数+index，index从0开始
                    const serialNumber = (window.currentPageForColumn - 1) * window.pageSizeForColumn + index + 1;

                    const td = `<td>${serialNumber}</td>
                                <td>${row.columnName}</td>
                                <td>${row.columnDesc}</td>
                                <td>${row.displayMultiplier}</td>
                                <td>${controlName}</td>
                                <td>${enumValues}</td>`;
                    tr.innerHTML += td;

                    // 添加操作列
                    const actionTd = document.createElement("td");
                    actionTd.style.minWidth = "200px";
                    actionTd.style.maxWidth = "200px"

                    // id字段不能被修改删除
                    if (row.columnName !== "id") {
                        const modifyTableButton = document.createElement("button");
                        modifyTableButton.className = "btn btn-sm me-2 btn-warning";
                        modifyTableButton.textContent = "修改";
                        modifyTableButton.onclick = () => updateColumnDialog(row); // 直接传递对象
                        actionTd.appendChild(modifyTableButton);

                        const deleteTableButton = document.createElement("button");
                        deleteTableButton.className = "btn btn-sm me-2 btn-danger";
                        deleteTableButton.textContent = "删除";
                        deleteTableButton.onclick = () => deleteColumnDialog(row); // 直接传递对象
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
                renderPageControlsColumnManager(totalPage, response.data.packageNum);
                addListenerForColumnAllSelector();
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

function getControlNameForControlType(controlType) {
    if (controlType === "select") {
        return "下拉框";
    } else {
        return "输入框";
    }
}

function applyFilterForColumn() {
    let columnName = document.getElementById("filterColumnNameInput").value;
    let columnDesc = document.getElementById("filterColumnDescInput").value;
    let controlType = document.getElementById("filterColumnControlType").value;

    let searchFields = "columnName,columnDesc,controlType";
    let searchValues = `${columnName},${columnDesc},${controlType}`;

    window.searchFieldsForColumn = searchFields;
    window.searchValuesForColumn = searchValues;

    listColumns();
}

function addListenerForColumnAllSelector() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox-column');
    selectAllCheckbox.addEventListener('change', function () {
        const tableId = selectAllCheckbox.getAttribute('data-table-id');
        const rowCheckboxes = document.querySelectorAll(`.${tableId}.row-checkbox`);

        // 根据全选框状态联动所有行复选框
        rowCheckboxes.forEach(checkbox => {
            checkbox.checked = selectAllCheckbox.checked;
        });
    });
}

// 渲染分页控件
function renderPageControlsColumnManager(totalPages, currentPage) {
    const pageSelect = document.getElementById("currentPageSelectForColumn");
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
    document.getElementById("totalPageInfoForColumn").textContent = `总页数: ${totalPages}`;
}

function changePageForColumn() {
    const selectedPage = document.getElementById("currentPageSelectForColumn").value;
    const currentPage = parseInt(selectedPage);
    window.currentPageForColumn = currentPage;
    listColumns();
}

// 改变每页显示条数
function changePageSizeForColumn() {
    const selectedSize = document.getElementById("pageSizeSelectForColumn").value;
    const pageSize = parseInt(selectedSize);
    window.pageSizeForColumn = pageSize;
    window.currentPageForColumn = 1;  // 切换每页条数时，回到第一页
    listColumns();
}

/********************************* 新增列 *********************************/

function showAddColumnModal() {
    // 每次打开模态框时，清空所有输入框的值
    const form = document.getElementById("addColumnForm");
    resetForm(form);

    // 设置列宽倍数默认值为1
    document.getElementById('displayMultiplierAdd').value = "1";

    document.getElementById("saveColumnButton").onclick = () => saveColumn();
    // 模态框标题改为修改列
    document.getElementById("addColumnModalLabel").textContent = "新增列";
    // 显示弹框
    openModal("addColumnModal");
}

// 新增和修改均复用此接口，后端通过参数columnId是否存在做判断
function saveColumn(row) {
    const formData = new FormData(document.getElementById("addColumnForm"));
    const record = {};

    // 构建保存数据对象
    formData.forEach((value, key) => {
        record[key] = value;
    });

    // 参数校验
    if (!isValidForAddTableColumn(record.columnName, record.columnDesc, record.displayMultiplier, record.controlType, record.enumValues)) {
        return;
    }

    // 增加 tableId
    record.tableId = window.dataList.tableId;
    // 如果row不为空
    if (row) {
        record.columnId = row.columnId;
    }

    // 发起保存请求
    fetch(`/column/save`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(record)
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("保存成功！");
                console.log("保存成功:", response);
                // 刷新表格
                listColumns();
            } else {
                responseParse(response.resultCode, response.message, "列数据保存失败！");
            }
        })
        .catch(error => console.error("保存失败:", error))
        .finally(() => {
            // 关闭弹框
            closeModal("addColumnModal");
        });
}

/********************************* 修改列 *********************************/

function updateColumnDialog(row) {
    // 回显数据
    // 每次打开模态框时，回显输入框的值
    document.getElementById("columnNameAdd").value = row.columnName;
    document.getElementById("columnDescAdd").value = row.columnDesc;
    document.getElementById("displayMultiplierAdd").value = row.displayMultiplier;
    document.getElementById("controlTypeAdd").value = row.controlType;
    document.getElementById("enumValuesAdd").value = row.enumValues;

    // 显示弹框
    document.getElementById("saveColumnButton").onclick = () => saveColumn(row);
    // 模态框标题改为修改列
    document.getElementById("addColumnModalLabel").textContent = "修改列";
    openModal("addColumnModal");
}

/********************************* 删除列 *********************************/

function deleteColumnDialog(row) {
    document.getElementById("confirmDeleteButton").onclick = () => deleteColumnDialogAgain(row);
    openModal("deleteModal");
}

function deleteColumnDialogAgain(row) {
    closeModal("deleteModal");
    document.getElementById("confirmAgainDeleteButton").onclick = () => deleteColumn(row);
    openModal("againDeleteModal");
}

function deleteColumn(row) {
    if (row.columnId !== null) {
        // 发送请求删除这条记录
        fetch(`/column/delete?columnId=${row.columnId}&tableId=${row.tableId}`, {
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
                    listColumns();
                } else {
                    responseParse(response.resultCode, response.message, "删除失败！");
                }
            })
            .catch(error => console.error("删除失败:", error))
            .finally(() => {
                // 关闭模态框
                closeModal("againDeleteModal");
            });
    }
}

/********************************* 批量删除列 *********************************/

let selectedIdsForColumn = [];

// 批量删除
function showBatchDeleteColumnModal() {
    // 获取选中行的 ID
    selectedIdsForColumn = Array.from(document.querySelectorAll(".row-checkbox:checked"))
        .map(checkbox => checkbox.getAttribute("data-id"));
    if (selectedIdsForColumn.length === 0) {
        showAlert("请先选择要删除的记录！");
        return;
    }

    // 修改确认按钮的onclick
    // 添加点击事件
    document.getElementById("confirmBatchDelete").onclick = () => showBatchDeleteColumnModalAgain();

    // 显示模态框
    openModal("batchDeleteModal");
}

function showBatchDeleteColumnModalAgain() {
    closeModal("batchDeleteModal");
    document.getElementById("confirmAgainBatchDelete").onclick = () => batchDeleteColumn();
    openModal("againBatchDeleteModal");
}

// 批量删除确认按钮点击事件
function batchDeleteColumn() {
    if (selectedIdsForColumn.length > 0) {
        console.log("删除的 ID 列表：", selectedIdsForColumn);

        // 发送请求删除这条记录
        fetch(`/column/batchDelete?tableId=${window.dataList.tableId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selectedIdsForColumn)
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 200) {
                    showAlert("批量删除成功！");
                    // 刷新表格
                    listColumns();
                } else {
                    responseParse(response.resultCode, response.message, "批量删除失败！");
                }
            })
            .catch(error => console.error("批量删除失败：", error))
            .finally(() => {
                // 关闭模态框
                closeModal("againBatchDeleteModal");
                selectedIdsForColumn = [];
            });
    }
}
