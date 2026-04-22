/**
 * 用户管理相关操作
 */

// 初始化默认数据
function initUserArgs() {
    window.currentPageForUser = 1;
    window.pageSizeForUser = 15;
    window.searchFieldsForUser = "";
    window.searchValuesForUser = "";
    // 重置分页相关元素的值
    renderPageControlsUser(1, 1);
    // 每页显示条数
    document.getElementById("pageSizeSelect_user").value = 15;
}
initUserArgs();

/********************************* 获取列表 *********************************/

// 回显所有用户的数据
function listUser(){

    // 数据初始化
    let pageNum = window.currentPageForUser || 1;
    let pageSize = window.pageSizeForUser || 15;
    const searchFields = window.searchFieldsForUser || "";
    const searchValues = window.searchValuesForUser || "";

    // 构建请求的 URL，添加筛选条件
    const url = `/user/list?packageNum=${pageNum}&packageSize=${pageSize}&orderFields=userId&order=asc&searchFields=${searchFields}&search=${searchValues}`;

    // 获取所有列表的数据
    fetch(url, {
        method: "GET",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                // 渲染右侧表格数据
                const tableBody = document.getElementById("user-table-body");
                tableBody.innerHTML = ""; // 清空表格内容

                const content = response.data.content;
                console.log("当前的用户列表:", content);

                // 遍历内容数组，生成表格行
                content.forEach((row, index) => {
                    const tr = document.createElement("tr");

                    // 添加复选框列
                    const checkboxTd = document.createElement("td");
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.className = `row-checkbox userManager`;
                    checkbox.setAttribute("data-id", row.userId);
                    // admin账户不允许批量删除
                    if (row.userName !== "admin") {
                        checkboxTd.appendChild(checkbox);
                    }
                    tr.appendChild(checkboxTd);

                    // 计算序号的值：当前页码×每页行数+index，index从0开始
                    const serialNumber = (window.currentPageForUser - 1) * window.pageSizeForUser + index + 1;

                    const td = `<td>${serialNumber}</td>
                                <td>${row.userName}</td>
                                <td>${row.roles[0].roleDesc}</td>
                                <td>${row.createTime}</td>
                                <td>${row.updateTime}</td>`;
                    tr.innerHTML += td;

                    // 添加操作列
                    const actionTd = document.createElement("td");
                    actionTd.style.minWidth = "200px";
                    actionTd.style.maxWidth = "200px"
                    const modifyButton = document.createElement("button");
                    modifyButton.className = "btn btn-sm me-2 btn-warning";
                    modifyButton.textContent = "修改";
                    modifyButton.onclick = () => modifyUserDialog(row); // 直接传递对象

                    const resetPwdButton = document.createElement("button");
                    resetPwdButton.className = "btn btn-sm me-2 btn-warning";
                    resetPwdButton.textContent = "重置密码";
                    resetPwdButton.onclick = () => resetPwdDialog(row); // 直接传递对象

                    const deleteButton = document.createElement("button");
                    deleteButton.className = "btn btn-sm me-2 btn-danger";
                    deleteButton.textContent = "删除";
                    deleteButton.onclick = () => deleteUserDialog(row.userId);

                    // 如果是admin账户，不显示修改/重置密码/删除按钮
                    if (row.roles[0].roleName !== "admin") {
                        actionTd.appendChild(modifyButton);
                        actionTd.appendChild(resetPwdButton);
                        actionTd.appendChild(deleteButton);
                    }

                    tr.appendChild(actionTd);

                    // 将当前行添加到表格主体
                    tableBody.appendChild(tr);
                });
                // 返回数据：data{"content":[{...}],"packageNum":"1","packageSize":"15","totalNum":"7"}
                // 计算总页数
                let totalPage = Math.ceil(response.data.totalNum / response.data.packageSize);
                // 设置页码相关数据
                renderPageControlsUser(totalPage, response.data.packageNum);
                // 给全选框增加监听
                addListenerForUserAllSelector();
            } else if (response.resultCode === 500) {
                showAlert("用户列表查询失败！" + response.message);
            } else if (response.resultCode === 401) {
                showAlert("登录已失效，请重新登录！");
                window.location.href = "/";  // 跳转到登录页面
                return;
            } else {
                showAlert("用户列表查询失败！");
                console.error("请求失败:", response.message);
            }
        })
        .catch(error => console.error("请求出错:", error));
}

function changeUserPage() {
    const selectedPage = document.getElementById("currentPageSelect_user").value;
    const currentPage = parseInt(selectedPage);
    window.currentPageForUser = currentPage;
    listUser();
}

// 改变每页显示条数
function changeUserPageSize() {
    const selectedSize = document.getElementById("pageSizeSelect_user").value;
    const pageSize = parseInt(selectedSize);
    window.pageSizeForUser = pageSize;
    window.currentPageForUser = 1;  // 切换每页条数时，回到第一页
    listUser();
}

function addListenerForUserAllSelector() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox-user');
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
function renderPageControlsUser(totalPages, currentPage) {
    const pageSelect = document.getElementById("currentPageSelect_user");
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
    document.getElementById("totalPageInfo_user").textContent = `总页数: ${totalPages}`;
}

/********************************* 新增 *********************************/

// 添加按钮
function addUserData() {
    // 每次打开模态框时，清空所有输入框的值
    const form = document.getElementById("addUserForm");
    resetForm(form);

    // 显示弹框
    const addModal = new bootstrap.Modal(document.getElementById("addUserModal"));
    addModal.show();
}

// 重置表单内容
function resetForm(form) {
    // 获取表单中的所有输入元素
    const inputs = form.querySelectorAll("input, select");

    inputs.forEach(input => {
        // 清空文本输入框
        if (input.type === "text" || input.type === "password" || input.type === "email" || input.type === "number") {
            input.value = "";
        }

        // 将下拉框选择为第一个选项
        if (input.tagName.toLowerCase() === "select") {
            input.selectedIndex = 0;  // 将选项设置为第一个
        }
    });
}

function isValidForAddUserForm() {
    // 获取表单字段值
    const username = document.getElementById("usernameAdd").value;
    const password = document.getElementById("passwordAdd").value;
    const confirmPassword = document.getElementById("confirmPasswordAdd").value;

    // 定义错误消息容器
    const usernameError = document.getElementById("usernameAddError");
    const passwordError = document.getElementById("passwordAddError");
    const confirmPasswordError = document.getElementById("confirmPasswordAddError");

    // 清空之前的错误消息
    usernameError.textContent = "";
    passwordError.textContent = "";
    confirmPasswordError.textContent = "";

    // 校验规则
    const usernameRegex = /^[0-9a-zA-Z_]{4,20}$/;
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$/;

    let isValid = true;

    // 校验用户名
    if (!usernameRegex.test(username)) {
        usernameError.textContent = "用户名由字母、数字或下划线组成，长度4~20位";
        isValid = false;
    }

    // 校验密码
    if (!passwordRegex.test(password)) {
        passwordError.textContent = "密码至少包含一个数字、字母及特殊字符，长度8~20位";
        isValid = false;
    }

    // 校验确认密码
    if (password !== confirmPassword) {
        confirmPasswordError.textContent = "两次密码输入不一致";
        isValid = false;
    }
    return isValid;
}

// 发送请求保存数据
function saveUser() {
    // 校验数据，密码与确认密码必须相同
    const isValid = isValidForAddUserForm();
    // 如果未校验通过，不做任何处理
    if (!isValid) {
        return;
    }

    const formData = new FormData(document.getElementById("addUserForm"));
    const record = {};
    // 构建保存数据对象
    formData.forEach((value, key) => {
        // 确认密码不需要提交
        if (key !== "confirmPassword") {
            record[key] = value;
        }
    });
    // 发起保存请求
    fetch(`/user/save`, {
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
                listUser();
            } else if (response.resultCode === 500) {
                showAlert("保存失败！" + response.message);
            } else {
                showAlert("保存失败！");
            }
        })
        .catch(error => console.error("保存失败:", error))
        .finally(() => {
            // 关闭弹框
            const addModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("addUserModal"));
            addModal.hide();
        });
}

/********************************* 删除 *********************************/

function deleteUserDialog(id) {
    const deleteModal = new bootstrap.Modal(document.getElementById("deleteModal"));
    document.getElementById("confirmDeleteButton").onclick = () => deleteUser(id);
    deleteModal.show();
}

function deleteUser(deleteId) {
    if (deleteId !== null) {
        // 发送请求删除这条记录
        fetch(`/user/delete?recordId=${deleteId}`, {
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
                    listUser();
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

/********************************* 修改 *********************************/

let modifyUserId = null;

function modifyUserDialog(row) {

    // 回显当前数据
    document.getElementById("usernameModify").value = row.userName;
    document.getElementById("roleModify").value = row.roles[0].roleId;

    modifyUserId = row.userId;

    // 显示弹框
    const addModal = new bootstrap.Modal(document.getElementById("modifyUserModal"));
    addModal.show();

}

function modifyUser() {
    if (modifyUserId === null) {
        return;
    }
    const formData = new FormData(document.getElementById("modifyUserForm"));
    const record = {};
    // 构建保存数据对象
    formData.forEach((value, key) => {
        // 确认密码不需要提交
        record[key] = value;
    });

    // 发起保存请求
    fetch(`/user/update?userId=${modifyUserId}`, {
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
                const addModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("modifyUserModal"));
                addModal.hide();
                // 刷新表格
                listUser();
            } else if (response.resultCode === 500) {
                showAlert("修改失败！" + response.message);
            } else {
                showAlert("修改失败！");
            }
        })
        .catch(error => console.error("修改失败:", error));
}

/********************************* 批量修改 *********************************/
function showBatchModifyModalForUser() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox-user');
    const tableId = selectAllCheckbox.getAttribute('data-table-id');
    // 获取选中的行 ID
    const selectedIds = Array.from(document.querySelectorAll(`.${tableId}.row-checkbox:checked`))
        .map(checkbox => checkbox.getAttribute("data-id"));

    if (selectedIds.length === 0) {
        showAlert("请先选择至少一行数据！");
        return;
    }

    // 显示模态框
    const batchModifyModal = new bootstrap.Modal(document.getElementById("batchModifyModalForUser"));
    batchModifyModal.show();
}

function batchModifyForUser() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox-user');
    const tableId = selectAllCheckbox.getAttribute('data-table-id');
    // 获取选中的行 ID
    const selectedIds = Array.from(document.querySelectorAll(`.${tableId}.row-checkbox:checked`))
        .map(checkbox => checkbox.getAttribute("data-id"));

    const modifyValue = document.getElementById("roleBatchModify").value;

    // 向后台发送请求
    fetch(`/user/batchModify?roleId=${modifyValue}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(selectedIds)
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("批量修改成功！");
                // 刷新表格
                listUser();
            } else if (response.resultCode === 500) {
                showAlert("批量修改失败！" + response.message);
            } else {
                showAlert("批量修改失败！");
            }
        })
        .catch(error => {
            console.error("批量修改请求失败:", error);
        })
        .finally(() => {
            // 关闭弹框
            const batchModifyModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("batchModifyModalForUser"));
            batchModifyModal.hide();
        });
}

/********************************* 批量删除 *********************************/

let selectedIdsForUser = [];

// 批量删除
function batchDeleteModalForUser() {
    // 获取选中行的 ID
    selectedIdsForUser = Array.from(document.querySelectorAll(".row-checkbox:checked"))
        .map(checkbox => checkbox.getAttribute("data-id"));
    if (selectedIdsForUser.length === 0) {
        showAlert("请先选择要删除的记录！");
        return;
    }

    // 修改确认按钮的onclick
    // 添加点击事件
    const batchDeleteButton = document.getElementById("confirmBatchDelete");
    batchDeleteButton.onclick = () => batchDeleteUser();

    // 显示模态框
    const deleteModal = new bootstrap.Modal(document.getElementById("batchDeleteModal"));
    deleteModal.show();
}

// 批量删除确认按钮点击事件
function batchDeleteUser() {
    if (selectedIdsForUser.length > 0) {
        console.log("删除的 ID 列表：", selectedIdsForUser);

        // 发送请求删除这条记录
        fetch(`/user/batchDelete`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selectedIdsForUser)
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 200) {
                    showAlert("批量删除成功！");
                    // 刷新表格
                    listUser();
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

// 按照条件搜索
function applyFilterForUser() {
    let userName = document.getElementById("filterUserNameInput").value;
    let roleId = document.getElementById("filterRoleInput").value;

    let searchFields = "userName,roleId";
    let searchValues = `${userName},${roleId}`;

    window.searchFieldsForUser = searchFields;
    window.searchValuesForUser = searchValues;

    listUser();
}

function resetPwdDialog(row) {

    // 每次打开弹框时清空表单的值
    document.getElementById("passwordReset").value = "";
    document.getElementById("confirmPasswordReset").value = "";

    const resetPwdModal = new bootstrap.Modal(document.getElementById("resetPwdModal"));
    document.getElementById("resetPwdButton").onclick = () => resetPwd(row);
    resetPwdModal.show();
}

function isValidForResetPwdForm(oper) {
    let password = null;
    let confirmPassword = null;
    let passwordError = null;
    let confirmPasswordError = null;

    if (oper === "reset") {
        // 获取表单字段值
        password = document.getElementById("passwordReset").value;
        confirmPassword = document.getElementById("confirmPasswordReset").value;

        // 定义错误消息容器
        passwordError = document.getElementById("passwordResetError");
        confirmPasswordError = document.getElementById("confirmPasswordResetError");
    } else {
        // 获取修改个人密码的表单字段值
        // 获取表单字段值
        password = document.getElementById("newPasswordSet").value;
        confirmPassword = document.getElementById("confirmPasswordSet").value;

        // 定义错误消息容器
        passwordError = document.getElementById("newPasswordSetError");
        confirmPasswordError = document.getElementById("confirmPasswordSetError");
    }
    // 获取表单字段值

    // 清空之前的错误消息
    passwordError.textContent = "";
    confirmPasswordError.textContent = "";

    // 校验规则
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$/;

    let isValid = true;

    // 校验密码
    if (!passwordRegex.test(password)) {
        passwordError.textContent = "密码至少包含一个数字、字母及特殊字符，长度8~20位";
        isValid = false;
    }

    // 校验确认密码
    if (password !== confirmPassword) {
        confirmPasswordError.textContent = "两次密码输入不一致";
        isValid = false;
    }
    return isValid;
}

function resetPwd(row) {
    // 校验数据，密码与确认密码必须相同
    const isValid = isValidForResetPwdForm("reset");
    // 如果未校验通过，不做任何处理
    if (!isValid) {
        return;
    }

    const passwordValue = document.getElementById("passwordReset").value;
    const password = encodeURIComponent(`${passwordValue}`);

    // 发起保存请求
    fetch(`/user/resetPwd?userId=${row.userId}&password=${password}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("重置成功！");
                console.log("重置成功:", response);

                // 刷新表格
                listUser();
            } else if (response.resultCode === 500) {
                showAlert("重置失败！" + response.message);
            } else {
                showAlert("重置失败！");
            }
        })
        .catch(error => console.error("重置失败:", error))
        .finally(() => {
            // 关闭弹框
            const addModal = bootstrap.Modal.getOrCreateInstance(document.getElementById("resetPwdModal"));
            addModal.hide();
        });
}

// 设置个人密码
function setPwd() {

    const oldPasswordValue = document.getElementById("oldPasswordSet").value;
    const passwordValue = document.getElementById("newPasswordSet").value;

    // 校验数据，旧密码也要使用密码校验
    // 校验规则
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$/;
    // 校验密码
    const oldPasswordError = document.getElementById("passwordOldError");
    oldPasswordError.textContent = "";
    if (!passwordRegex.test(oldPasswordValue)) {
        oldPasswordError.textContent = "密码至少包含一个数字、字母及特殊字符，长度8~20位";
        return;
    }

    // 校验数据，密码与确认密码必须相同
    const isValid = isValidForResetPwdForm("set");
    // 如果未校验通过，不做任何处理
    if (!isValid) {
        return;
    }

    // 获取当前登录用户信息
    if (window.loginUser !== null) {
        const userId = window.loginUser.userId;
        const password = encodeURIComponent(`${passwordValue}`);
        const oldPassword = encodeURIComponent(`${oldPasswordValue}`);
        // 发起保存请求
        fetch(`/user/setPwd?userId=${userId}&oldPassword=${oldPassword}&password=${password}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 200) {
                    showAlert("密码修改成功，请重新登录！");
                    const alertModal = document.getElementById('customAlertModal');
                    alertModal.addEventListener("hidden.bs.modal", () => {
                        window.location.href = "/"; // 跳转到登录页面
                    });
                    console.log("修改成功:", response);
                    //window.location.href = "/";  // 跳转到登录页面
                } else if (response.resultCode === 500) {
                    showAlert("密码修改失败！" + response.message);
                } else {
                    showAlert("密码修改失败！");
                }
            })
            .catch(error => console.error("密码修改失败:", error));
    } else {
        // 登录失效，请重新登录
        showAlert("登录已失效，请重新登录！");
        window.location.href = "/";  // 跳转到登录页面
        return;
    }
}

// 增加监听：隐藏/显示密码
function addListenerForEyeButton() {
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', function () {
            const inputId = this.getAttribute('data-target'); // 获取目标输入框的 ID
            const input = document.getElementById(inputId);
            const icon = this.querySelector('i'); // 获取按钮中的图标

            // 切换密码显示/隐藏
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('bi-eye-slash');
                icon.classList.add('bi-eye');
            } else {
                input.type = 'password';
                icon.classList.remove('bi-eye');
                icon.classList.add('bi-eye-slash');
            }
        });
    });
}
addListenerForEyeButton();