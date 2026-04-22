/**
 * 单个表格界面的按钮操作
 */

/********************************* 获取当前表的数据 *********************************/

function initTableDataArgs() {
    // 初始化默认数据
    window.currentPage = 1;
    window.pageSize = 15;
    window.searchFields = "";
    window.searchValues = "";
    // 重置分页相关元素的值
    renderPageControls(1, 1);
    // 每页显示条数
    document.getElementById("pageSizeSelect").value = 15;
}
initTableDataArgs();

function loadTableData() {

    // 获取当前表格的信息
    const selectedData = window.dataList;
    // 回显表头到表格中
    const tableColumnList = selectedData.tableColumnList;

    // 获取表头容器
    const tableHeader = document.getElementById("table-header");
    tableHeader.innerHTML = ""; // 清空表头内容

    // 添加表头复选框列
    const selectAllTh = document.createElement("th");
    const selectAllCheckbox = document.createElement("input");
    selectAllCheckbox.type = "checkbox";
    selectAllCheckbox.id = "select-all-checkbox"; // 设置 ID 方便后续绑定事件
    selectAllCheckbox.addEventListener("change", function () {
        // 获取所有行复选框
        const rowCheckboxes = document.querySelectorAll(".row-checkbox");
        rowCheckboxes.forEach(checkbox => {
            checkbox.checked = selectAllCheckbox.checked; // 联动操作
        });
    });
    selectAllTh.appendChild(selectAllCheckbox);
    tableHeader.appendChild(selectAllTh);

    // 生成序号列表头
    const th = document.createElement("th");
    th.textContent = "序号";
    th.style.minWidth = `100px`;
    th.style.maxWidth = `200px`;
    tableHeader.appendChild(th);

    // 动态生成其他表头字段
    tableColumnList.forEach(column => {
        if (column.columnName !== "id") {
            const th = document.createElement("th");
            th.textContent = column.columnDesc;

            // 动态计算宽度并设置
            const minWidth = 100 * column.displayMultiplier;
            const maxWidth = 200 * column.displayMultiplier;
            th.style.minWidth = `${minWidth}px`;
            th.style.maxWidth = `${maxWidth}px`;

            tableHeader.appendChild(th);
        }
    });

    // 添加最后一列的操作列标题
    const actionTh = document.createElement("th");
    actionTh.textContent = "操作";
    actionTh.style.minWidth = "200px";
    actionTh.style.maxWidth = "200px";
    tableHeader.appendChild(actionTh);

    // 发起获取表数据的请求，携带 tableName
    const tableName = selectedData.tableName;

    let pageNum = window.currentPage || 1;
    let pageSize = window.pageSize || 15;
    const searchFields = window.searchFields || "";
    const searchValues = window.searchValues || "";

    // 构建请求的 URL，添加筛选条件
    const url = `/table/getTableData?packageNum=${pageNum}&packageSize=${pageSize}&orderFields=id&order=asc&searchFields=${searchFields}&search=${searchValues}&tableName=${tableName}`;

    // 获取所有列表的数据
    fetch(url, {
        method: "GET",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                // 渲染右侧表格数据
                const tableBody = document.getElementById("table-body");
                tableBody.innerHTML = ""; // 清空表格内容

                const tableColumnList = selectedData.tableColumnList;
                const content = response.data.content;
                console.log("当前表格的数据列表:", content);

                // 遍历内容数组，生成表格行
                content.forEach((row, index) => {
                    const tr = document.createElement("tr");

                    // 添加复选框列
                    const checkboxTd = document.createElement("td");
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.className = "row-checkbox";
                    checkbox.setAttribute("data-id", row.id);
                    checkboxTd.appendChild(checkbox);
                    tr.appendChild(checkboxTd);

                    // 计算序号的值：当前页码×每页行数+index，index从0开始
                    const serialNumber = (window.currentPage - 1) * window.pageSize + index + 1;
                    // 序号在第一列
                    const td = document.createElement("td");
                    td.textContent = serialNumber;
                    td.style.minWidth = "100px";
                    td.style.maxWidth = "200px"
                    tr.appendChild(td);

                    // 动态生成表格数据列
                    tableColumnList.forEach(column => {
                        // 跳过id
                        if (column.columnName !== "id") {
                            const td = document.createElement("td");
                            td.textContent = row[column.columnName] || ""; // 填充数据，如果字段不存在则为空
                            td.style.minWidth = "100px";
                            td.style.maxWidth = "200px"
                            tr.appendChild(td);
                        }
                    });

                    // 添加操作列
                    const actionTd = document.createElement("td");
                    actionTd.style.minWidth = "200px";
                    actionTd.style.maxWidth = "200px"
                    const modifyButton = document.createElement("button");
                    modifyButton.className = "btn btn-sm me-2 btn-warning";
                    modifyButton.textContent = "修改";
                    modifyButton.onclick = () => modify(row); // 直接传递对象

                    const deleteButton = document.createElement("button");
                    deleteButton.className = "btn btn-sm me-2 btn-danger";
                    deleteButton.textContent = "删除";
                    deleteButton.onclick = () => deleteItem(row.id);

                    actionTd.appendChild(modifyButton);
                    actionTd.appendChild(deleteButton);

                    tr.appendChild(actionTd);

                    // 将当前行添加到表格主体
                    tableBody.appendChild(tr);
                });
                // 返回数据：data{"content":[{...}],"packageNum":"1","packageSize":"15","totalNum":"7"}
                // 计算总页数
                let totalPage = Math.ceil(response.data.totalNum / response.data.packageSize);
                // 设置页码相关数据
                renderPageControls(totalPage, response.data.packageNum);
            } else if (response.resultCode === 401) {
                showAlert("登录已失效，请重新登录！");
                window.location.href = "/";  // 跳转到登录页面
                return;
            } else if (response.resultCode === 500) {
                showAlert("表数据查询失败！" + response.message);
            } else {
                showAlert("表数据查询失败")
                console.error("表数据查询失败");
            }
        })
        .catch(error => console.error("表数据查询出错:", error));

}

// 渲染分页控件
function renderPageControls(totalPages, currentPage) {
    const pageSelect = document.getElementById("currentPageSelect");
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
    document.getElementById("totalPageInfo").textContent = `总页数: ${totalPages}`;
}

// 改变页码
function changePage() {
    const selectedPage = document.getElementById("currentPageSelect").value;
    let currentPage = parseInt(selectedPage);
    window.currentPage = currentPage;
    loadTableData();
}

// 改变每页显示条数
function changePageSize() {
    const selectedSize = document.getElementById("pageSizeSelect").value;
    let pageSize = parseInt(selectedSize);
    window.pageSize = pageSize;
    let currentPage = 1; // 切换每页条数时，回到第一页
    window.currentPage = currentPage;
    loadTableData();
}

/********************************* 修改 *********************************/

// 修改按钮
function modify(rowData) {
    // 获取模态框实例
    const addModal = new bootstrap.Modal(document.getElementById("addModal"));

    // 修改标题为 "修改记录"
    document.getElementById("addModalLabel").textContent = "修改记录";

    // 获取表单容器
    const recordForm = document.getElementById("addForm");
    recordForm.innerHTML = ""; // 清空表单内容

    // 获取当前列名列表
    const selectedData = window.dataList;

    // 获取表中列的信息
    const tableColumnList = selectedData.tableColumnList;

    let row; // 用于存储每行的容器

    // 遍历列名，生成对应的输入框，并设置默认值
    tableColumnList.forEach((column, index) => {
        if (column.columnName === "id") {
            return;
        }
        // 如果当前行没有创建或已满3个输入框，创建一个新行
        if ((index - 1) % 3 === 0) {
            row = document.createElement("div");
            row.className = "row mb-3"; // 每行最多3个输入框，增加mb-3用于间距
            recordForm.appendChild(row);
        }

        const formGroup = document.createElement("div");
        formGroup.className = "col-md-4 d-flex align-items-center";

        const label = document.createElement("label");
        label.className = "form-label me-2 text-nowrap"; // 添加text-nowrap确保label的文本不换行
        label.style.minWidth = "120px"; // 设置最小宽度
        label.style.maxWidth = "150px"; // 设置最大宽度，保持一致
        label.textContent = column.columnDesc;
        label.setAttribute("for", column.columnDesc);
        formGroup.appendChild(label);

        if (column.controlType === "input") {
            // 创建input
            const input = document.createElement("input");
            input.type = "text";
            input.className = "form-control";
            input.style.width = "200px"; // 设置固定宽度，确保所有输入框一致
            //input.setAttribute("name", column.columnDesc);
            input.id = column.columnName;
            input.name = column.columnName;
            input.value = rowData[column.columnName] || ""; // 回显数据
            if (column.columnName === "id") {
                input.disabled = true;
            }
            formGroup.appendChild(input);
        } else if (column.controlType === "select") {
            // 创建下拉框
            const select = document.createElement("select");
            select.className = "form-select";
            select.style.width = "200px"; // 设置固定宽度，确保一致
            select.name = column.columnName;
            select.id=column.columnName;

            // 将 enumValues 转换为列表
            const enumValues = column.enumValues.split(","); // 假设枚举值以英文逗号分隔
            enumValues.forEach((value, index) => {
                const option = document.createElement("option");
                option.value = value.trim();
                option.textContent = value.trim();
                /*if (index === 0) {
                    option.selected = true; // 默认选择第一个值
                }*/
                select.appendChild(option); // 将选项添加到下拉框
            });
            select.value=rowData[column.columnName] || ""; // 回显数据

            formGroup.appendChild(select); // 将下拉框添加到表单组
        }
        row.appendChild(formGroup);
    });

    // 检查最后一行是否需要补齐空白占位符
    const lastRowChildren = row.children.length;
    if (lastRowChildren < 3) {
        for (let i = 0; i < 3 - lastRowChildren; i++) {
            const placeholder = document.createElement("div");
            placeholder.className = "col-md-4 d-flex align-items-center"; // 添加占位元素，保持布局一致

            // 添加空白内容占位符，label 和 input 都不可见
            placeholder.innerHTML = `
                    <label class="form-label me-2" style="visibility: hidden; min-width: 120px;">&nbsp;</label>
                    <input type="text" class="form-control" style="visibility: hidden; flex-grow: 1; min-width: 120px;" />
                `;

            row.appendChild(placeholder);
        }
    }

    // 为保存按钮动态绑定当前记录的 id（如果需要）
    const saveButton = document.getElementById("saveButton");
    saveButton.setAttribute("data-id", rowData.id);

    // 显示模态框
    addModal.show();
}

/********************************* 删除 *********************************/

// 全局变量存储要删除的记录 ID
//let deleteId = null;

// 删除按钮
function deleteItem(id) {
    //deleteId = id; // 保存 ID
    const deleteModal = new bootstrap.Modal(document.getElementById("deleteModal"));
    document.getElementById("confirmDeleteButton").onclick = () => deleteTableData(id);
    deleteModal.show();
}

function deleteTableData(deleteId) {
    if (deleteId !== null) {
        // 获取当前列名列表
        const selectedData = window.dataList;
        // 发送请求删除这条记录
        fetch(`/table/deleteData?tableName=${selectedData.tableName}&recordId=${deleteId}`, {
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
                    loadTableData();
                } else {
                    showAlert("删除失败！");
                }
            })
            .catch(error => console.error("删除失败:", error))
            .finally(() => {
                // 关闭模态框
                const deleteModal = bootstrap.Modal.getInstance(document.getElementById("deleteModal"));
                deleteModal.hide();
                //deleteId = null; // 重置 ID
            });
    }
};

/********************************* 批量删除 *********************************/

// 全局变量存储选中的 ID 列表
let selectedIds = [];

// 批量删除
function showBatchDeleteTableDataModal() {
    // 获取选中行的 ID
    selectedIds = Array.from(document.querySelectorAll(".row-checkbox:checked"))
        .map(checkbox => checkbox.getAttribute("data-id"));
    if (selectedIds.length === 0) {
        showAlert("请先选择要删除的记录！");
        return;
    }

    // 添加点击事件
    const batchDeleteButton = document.getElementById("confirmBatchDelete");
    batchDeleteButton.onclick = () => batchDeleteTableData();

    // 显示模态框
    const deleteModal = new bootstrap.Modal(document.getElementById("batchDeleteModal"));
    deleteModal.show();
}

// 批量删除确认按钮点击事件
function batchDeleteTableData() {
    if (selectedIds.length > 0) {
        console.log("删除的 ID 列表：", selectedIds);

        // 获取当前列名列表
        const selectedData = window.dataList;
        // 发送请求删除这条记录
        fetch(`/table/batchDeleteData?tableName=${selectedData.tableName}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selectedIds)
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 200) {
                    showAlert("批量删除成功！");
                    // 刷新表格或移除已删除的行
                    loadTableData();
                } else {
                    showAlert("批量删除失败！");
                }
            })
            .catch(error => console.error("批量删除失败：", error))
            .finally(() => {
                // 关闭模态框
                const deleteModal = bootstrap.Modal.getInstance(document.getElementById("batchDeleteModal"));
                deleteModal.hide();
                selectedIds = [];
            });
    }
}

/********************************* 批量修改 *********************************/

function showBatchModifyModal() {

    // 获取选中的行 ID
    const selectedIds = Array.from(document.querySelectorAll(".row-checkbox:checked"))
        .map(checkbox => checkbox.getAttribute("data-id"));

    if (selectedIds.length === 0) {
        showAlert("请先选择至少一行数据！");
        return;
    }

    // 获取当前列名列表
    const selectedData = window.dataList;

    // 获取表中列的信息
    const tableColumnList = selectedData.tableColumnList;

    const modifyFieldSelect = document.getElementById("modifyField");

    // 清空之前的选项
    modifyFieldSelect.innerHTML = "";

    // 填充下拉框，排除 "id" 字段
    tableColumnList.forEach((column, index) => {
        if (column.columnName !== "id") {
            const option = document.createElement("option");
            option.value = column.columnName;
            option.textContent = column.columnDesc;
            modifyFieldSelect.appendChild(option);
        }
    });

    // 清空上一次填写的修改值内容
    document.getElementById("modifyValue").value = "";

    // 显示模态框
    const batchModifyModal = new bootstrap.Modal(document.getElementById("batchModifyModal"));
    batchModifyModal.show();
}

function batchModify() {
    // 获取选中的行 ID
    const selectedIds = Array.from(document.querySelectorAll(".row-checkbox:checked"))
        .map(checkbox => checkbox.getAttribute("data-id"));

    // 获取用户输入的字段和修改值
    const modifyField = document.getElementById("modifyField").value;
    const modifyValue = document.getElementById("modifyValue").value;

    if (!modifyField || !modifyValue) {
        showAlert("请填写完整的修改字段和值！");
        return;
    }

    // 构造请求体
    const payload = {
        ids: selectedIds,
        field: modifyField,
        value: modifyValue
    };

    // 获取当前列名列表
    const selectedData = window.dataList;
    // 向后台发送请求
    fetch(`/table/batchModifyData?tableName=${selectedData.tableName}&field=${modifyField}&value=${modifyValue}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(selectedIds)
    })
        .then(response => response.json())
        .then(data => {
            if (data.resultCode === 200) {
                showAlert("批量修改成功！");
                // 刷新数据
                loadTableData();
            } else {
                showAlert("批量修改失败：" + data.message);
            }
        })
        .catch(error => {
            console.error("批量修改请求失败:", error);
        })
        .finally(() => {
            // 关闭弹框
            const batchModifyModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("batchModifyModal"));
            batchModifyModal.hide();
        });
}

/********************************* 过滤器 *********************************/

function initFilter(optionIds) {
    // 假设 columnList 从后端返回，包含表的字段名
    // 获取当前列名列表
    const selectedData = window.dataList;

    // 获取表中列的信息
    const tableColumnList = selectedData.tableColumnList;

    // 初始化下拉框
    const filterSelect = document.getElementById("filterSelect");
    // 清空所有子元素
    while (filterSelect.firstChild) {
        filterSelect.removeChild(filterSelect.firstChild);
    }

    // 添加默认选项
    const defaultOption = document.createElement("option");
    defaultOption.value = "";
    defaultOption.textContent = "选择过滤字段";
    filterSelect.appendChild(defaultOption);
    // 添加可选项
    tableColumnList.forEach((column, index) => {
        if (column.columnName !== "id" && (optionIds.length === 0 || optionIds.includes(column.columnName))) {
            const option = document.createElement("option");
            option.value = column.columnName;
            option.textContent = column.columnDesc;
            filterSelect.appendChild(option);
        }
    });
}

// 过滤器区域的容器
const filterArea = document.getElementById("filter-area");

function addFilter() {
    const selectedColumn = filterSelect.value; // 获取选中字段的值
    const selectedText = filterSelect.options[filterSelect.selectedIndex].text; // 获取选中字段的文本

    if (selectedColumn) {
        // 创建单个筛选条件的容器
        const filterGroup = document.createElement("div");
        filterGroup.id = `filter-${selectedColumn}`;
        filterGroup.classList.add("d-flex", "align-items-center", "me-3"); // 增加右边距，确保间距统一
        filterGroup.style.flex = "0 0 22%"; // 每个筛选条件占 22%，确保一行最多放 4 个
        filterGroup.style.gap = "10px";

        // 生成筛选条件的 HTML
        filterGroup.innerHTML = `
                <label class="me-2 text-nowrap" style="min-width: 100px;" title="${selectedText}">
                    ${selectedText}:
                </label>
                <input type="text" id="filter-text-${selectedColumn}" class="form-control" placeholder="请输入${selectedText}的过滤值">
                <button class="btn btn-danger text-nowrap ms-2" style="white-space: nowrap;" onclick="removeFilter('${selectedColumn}')">删除</button>
            `;

        // 将当前筛选条件添加到最后一行
        filterArea.appendChild(filterGroup);

        // 从下拉框中移除已选字段
        const optionToRemove = filterSelect.querySelector(`option[value='${selectedColumn}']`);
        if (optionToRemove) {
            filterSelect.removeChild(optionToRemove);
        }
    }

    // 确保搜索按钮始终在所有筛选条件的末尾
    ensureSearchButton();
}

function ensureSearchButton() {
    let searchButtonContainer = document.getElementById("search-button-container");
    if (!searchButtonContainer) {
        searchButtonContainer = document.createElement("div");
        searchButtonContainer.id = "search-button-container";
        // "d-flex" :启用 flexbox 布局，使 searchButtonContainer 的子元素（如搜索按钮）以灵活的方式排列。默认情况下，flexbox 子元素会排列在一行内。
        // "justify-content-end" :设置 flexbox 容器内子元素的水平对齐方式为 右对齐。这意味着搜索按钮会被推到容器的最右侧。
        searchButtonContainer.classList.add("d-flex", "justify-content-end");

        const searchButton = document.createElement("button");
        searchButton.id = "search-button";
        searchButton.className = "btn btn-primary text-nowrap";
        searchButton.textContent = "搜索";
        searchButton.style.minWidth = "60px"; // 确保按钮宽度一致
        searchButton.style.height = "38px";
        searchButton.onclick = applyFilter; // 绑定搜索事件

        searchButtonContainer.appendChild(searchButton);
        filterArea.appendChild(searchButtonContainer);
    } else {
        // 确保按钮容器在最后
        filterArea.appendChild(searchButtonContainer);
    }
}

// 移除过滤器
function removeFilter(column) {
    // 删除对应的过滤器行
    const filterRow = document.getElementById(`filter-${column}`);
    filterArea.removeChild(filterRow);

    // 恢复下拉框中的选项
    const option = document.createElement("option");
    option.value = column;

    // 初始化过滤器，排除已经添加的筛选项

    const filterSelect = document.getElementById("filterSelect");

    // 获取所有 option 的 id，组成列表
    let optionIds = Array.from(filterSelect.options) // 将 HTMLCollection 转为数组
        .map(option => option.value || null) // 只提取 option 的 value，若无则为 null
        .filter(value => value); // 过滤掉空值

    optionIds = [...optionIds, column];

    initFilter(optionIds);
    // 如果筛选条件的个数为0，则删除搜索按钮
    // 检查筛选区域是否仅剩搜索按钮
    checkAndRemoveSearchButton();
}

// 检查并删除搜索按钮
function checkAndRemoveSearchButton() {
    const filterRows = filterArea.querySelectorAll("div");
    const searchButton = document.getElementById("search-button-container");

    // 如果没有任何过滤条件行，则移除搜索按钮
    if (filterRows.length === 1 && searchButton) {
        searchButton.remove();
    }
    // 每次删除筛选条件时，重新加载搜索按钮的点击事件
    applyFilter();
}

// 获取所有选中的过滤器条件
function getFilters() {
    // 获取当前列名列表
    const selectedData = window.dataList;

    // 获取表中列的信息
    const tableColumnList = selectedData.tableColumnList;

    const filters = [];
    tableColumnList.forEach(column => {
        let columnName = column.columnName;
        const filterInput = document.getElementById(`filter-text-${columnName}`);
        if (filterInput) {
            const filterValue = filterInput.value;

            if (filterValue) {
                filters.push({ columnName, value: filterValue });
            }
        }
    });
    return filters;
}

// 应用过滤器
function applyFilter() {
    const filters = getFilters();

    let searchFields = "";
    let searchValues = "";

    filters.forEach(filter => {
        if (searchFields) {
            searchFields += ",";
            searchValues += ",";
        }
        searchFields += filter.columnName;
        searchValues += filter.value;
    });

    window.searchFields = searchFields;
    window.searchValues = searchValues;
    // 调用 loadTableData
    loadTableData();
}

// 调整弹框宽度和高度动态适应内容
function adjustModalSize() {
    const modalContent = document.querySelector('.modal-content');
    const modalBody = document.querySelector('.modal-body');

    modalContent.style.width = "auto";
    modalBody.style.maxHeight = "80vh"; // 防止内容过多溢出
    modalBody.style.overflowY = "auto"; // 超出部分滚动
}

// 添加按钮
function addData() {
    const addForm = document.getElementById("addForm");

    // 修改标题为 "修改记录"
    document.getElementById("addModalLabel").textContent = "新增记录";

    addForm.innerHTML = ""; // 清空表单内容

    // 动态生成输入字段
    const selectedData = window.dataList;

    let row; // 用于存储每行的容器

    // 获取表中列的信息
    const tableColumnList = selectedData.tableColumnList;
    if (tableColumnList.length == 1 && tableColumnList[0].columnName === "id") {
        showAlert("无可用字段！");
        return;
    }

    tableColumnList.forEach((column, index) => {
        // 不显示id，后端自动生成
        if (column.columnName === "id") {
            return;
        }
        // 如果当前行没有创建或已满3个输入框，创建一个新行
        // 如果当前行没有创建或已满3个输入框，创建一个新行
        if (!row || row.children.length >= 3) { // 确保每行最多3个输入框
            row = document.createElement("div");
            row.className = "row mb-3"; // 每行最多3个输入框，增加mb-3用于间距
            addForm.appendChild(row);
        }

        // 创建formGroup和输入框
        const formGroup = document.createElement("div");
        formGroup.className = "col-md-4 d-flex align-items-center"; // 每个输入框占据1/3宽度，且label和input在同一行

        // 创建label
        const label = document.createElement("label");
        label.className = "form-label me-2 text-nowrap"; // 添加text-nowrap确保label的文本不换行
        label.style.minWidth = "120px"; // 设置最小宽度
        label.style.maxWidth = "150px"; // 设置最大宽度，保持一致
        label.textContent = column.columnDesc + ":";

        // 将label和input放入formGroup并添加到row
        formGroup.appendChild(label);

        if (column.controlType === "input") {
            // 创建input
            const input = document.createElement("input");
            input.type = "text";
            input.className = "form-control";
            input.style.width = "200px"; // 设置固定宽度，确保所有输入框一致
            input.setAttribute("name", column.columnDesc);
            formGroup.appendChild(input);
        } else if (column.controlType === "select") {
            // 创建下拉框
            const select = document.createElement("select");
            select.className = "form-select";
            select.style.width = "200px"; // 设置固定宽度，确保一致
            select.setAttribute("name", column.columnDesc);

            // 将 enumValues 转换为列表
            const enumValues = column.enumValues.split(","); // 假设枚举值以英文逗号分隔
            enumValues.forEach((value, index) => {
                const option = document.createElement("option");
                option.value = value.trim();
                option.textContent = value.trim();
                if (index === 0) {
                    option.selected = true; // 默认选择第一个值
                }
                select.appendChild(option); // 将选项添加到下拉框
            });

            formGroup.appendChild(select); // 将下拉框添加到表单组
        }

        row.appendChild(formGroup);
    });

    // 检查最后一行是否需要补齐空白占位符
    const lastRowChildren = row.children.length;
    if (lastRowChildren < 3) {
        for (let i = 0; i < 3 - lastRowChildren; i++) {
            const placeholder = document.createElement("div");
            placeholder.className = "col-md-4 d-flex align-items-center"; // 添加占位元素，保持布局一致

            // 添加空白内容占位符，label 和 input 都不可见
            placeholder.innerHTML = `
                    <label class="form-label me-2" style="visibility: hidden; min-width: 120px;">&nbsp;</label>
                    <input type="text" class="form-control" style="visibility: hidden; flex-grow: 1; min-width: 120px;" />
                `;

            row.appendChild(placeholder);
        }
    }

    // 显示弹框
    adjustModalSize();
    const addModal = new bootstrap.Modal(document.getElementById("addModal"));
    addModal.show();
}

function hideSaveModal(){
    // 隐藏保存/修改框时，将绑定的id置为空
    const saveButton = document.getElementById("saveButton");
    saveButton.setAttribute("data-id", ""); // 设置 data-id 属性为空
}

// 保存新增记录
function saveRecord() {
    const selectedData = window.dataList;
    const formData = new FormData(document.getElementById("addForm"));
    const record = {};
    // 获取字段列表

    // 构建保存数据对象
    formData.forEach((value, key) => {
        record[key] = value;
    });

    const saveButton = document.getElementById("saveButton");
    const recordId = saveButton.getAttribute("data-id"); // 获取绑定的记录 ID

    if (recordId) {
        updateData(recordId, record, selectedData.tableName);
    } else {
        saveData(record, selectedData.tableName);
    }
}

function updateData(recordId, record, tableName) {
    // 发起保存请求（保存接口为 /table/updateData）
    fetch(`/table/updateData?tableName=${tableName}&recordId=${recordId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(record)
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("修改成功！");
                console.log("修改成功:", response);
                // 关闭弹框
                const addModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("addModal"));
                addModal.hide();
                const saveButton = document.getElementById("saveButton");
                saveButton.setAttribute("data-id", ""); // 设置 data-id 属性为空
                // 刷新表格
                loadTableData();
            } else {
                showAlert("修改失败！");
            }
        })
        .catch(error => console.error("修改失败:", error));
}

function saveData(record, tableName) {
    console.log("保存数据:", record);
    // 发起保存请求（保存接口为 /table/saveData）
    fetch(`/table/saveData?tableName=${tableName}`, {
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
                // 关闭弹框
                const addModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("addModal"));
                addModal.hide();
                // 刷新表格
                loadTableData();
            } else {
                showAlert("保存失败！");
            }
        })
        .catch(error => console.error("保存失败:", error));
}