/**
 * home界面的初始化
 */

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");

    if (!token) {
        showAlert("登录已失效，请重新登录！");
        window.location.href = "/";  // 跳转到登录页面
        return;
    } else {
        // 向后端发送一个通用请求，确认当前token是否有效
        fetch("/tokenVerify", {
            method: "GET",
            headers: {
                "token": token // 将 Token 添加到请求头
            }
        })
            .then(response => response.json())
            .then(response => {
                if (response.resultCode === 401) {
                    showAlert("登录已失效，请重新登录！");
                    window.location.href = "/";  // 跳转到登录页面
                    return;
                }
            })
            .catch(error => {
                console.error("Error:", error);
            });
    }
});

// 加载当前用户信息，根据用户权限区分加载的菜单项
function loadCurrentUser() {
    fetch(`/getLoginUser`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                // 回显右上角当前登录用户名称
                document.getElementById("username").textContent = response.data.userName;
                window.loginUser = response.data;
                // 控制左侧菜单项显示
                controlMenuVisibility(response.data);
            } else if (response.resultCode === 401) {
                showAlert("登录已失效，请重新登录！");
                window.location.href = "/";  // 跳转到登录页面
                return;
            } else if (response.resultCode === 500) {
                showAlert("当前登录用户查询失败！" + response.message);
            } else {
                showAlert("查询失败！");
            }
        })
        .catch(error => console.error("获取当前登录用户名称失败:", error));
}

// 根据权限控制菜单显示
function controlMenuVisibility(userData) {
    const menuItems = document.querySelectorAll("#tabList .list-group-item");

    const userGroup = userData.roles[0].roleName;

    menuItems.forEach(item => {
        const role = item.getAttribute("data-role");
        let shouldDisplay = false;

        // 如果 userGroup 是 admin，显示 role 为 security 的菜单项
        if (userGroup === "admin" && role === "security") {
            shouldDisplay = true;
        }

        // 如果 userGroup 是 super，显示 role 为 manager 和 user 的菜单项
        if (userGroup === "super" && (role === "manager" || role === "user")) {
            shouldDisplay = true;
        }

        // 如果 userGroup 是 user(普通账户)，显示 role 为 user 的菜单项
        if (userGroup === "user" && role === "user") {
            shouldDisplay = true;
        }

        if (role === "all") {
            shouldDisplay = true;
        }

        // 根据最终判断设置菜单项的显示状态
        item.style.display = shouldDisplay ? "block" : "none";
    });
    // 查找第一个显示的菜单项
    const visibleMenuItem = Array.from(document.querySelectorAll(".list-group-item"))
        .find(item => item.offsetParent !== null); // 确保是可见的

    // 如果找到了可见的菜单项，触发点击事件
    if (visibleMenuItem) {
        visibleMenuItem.click();
    }
}

/**
 * 从后端下载 classpath 中的 PDF 帮助文档。
 * 说明：不能使用不带请求头的超链接直链，因为 Shiro 要求与其它接口一样在请求头携带 token；
 * 此处用 fetch 拉取二进制后再通过 Blob 触发浏览器保存（与 common.js 中对 fetch 的 token 注入配合使用）。
 */
function downloadHelpPdf() {
    const token = localStorage.getItem("token");
    if (!token) {
        showAlert("登录已失效，请重新登录！");
        window.location.href = "/";
        return;
    }
    fetch("/api/help/manual", {
        method: "GET",
        headers: {
            token: token
        }
    })
        .then(async (response) => {
            // 未登录或 token 失效时，后端返回 JSON，不能当 PDF 保存
            if (response.status === 401) {
                showAlert("登录已失效，请重新登录！");
                window.location.href = "/";
                return null;
            }
            if (!response.ok) {
                showAlert("帮助文档下载失败，请稍后重试。");
                return null;
            }
            return response.blob();
        })
        .then((blob) => {
            if (!blob) {
                return;
            }
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "datamanager使用.pdf";
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        })
        .catch((error) => {
            console.error("下载帮助文档失败:", error);
            showAlert("帮助文档下载失败，请检查网络后重试。");
        });
}

function logout() {
    // 发送退出登录请求
    fetch(`/logout`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => response.json())
        .then(response => {
            if (response.resultCode === 200) {
                showAlert("您已成功退出登录！");
                window.location.href = "/";
            }
        })
        .catch(error => console.error("退出登录失败:", error));
}

// 切换左侧标签页的动作
function switchTab(element) {
    // 移除所有菜单项的 active 类
    document.querySelectorAll(".list-group-item").forEach(item => {
        item.classList.remove("active");
    });

    // 为当前点击的菜单项添加 active 类
    element.classList.add("active");

    // 获取对应的内容区域 ID
    const targetId = element.getAttribute("data-target");

    // 隐藏所有内容区域
    document.querySelectorAll(".tab-pane").forEach(pane => {
        pane.classList.remove("active");
    });

    // 显示对应的内容区域
    document.getElementById(targetId).classList.add("active");

    if (targetId === "userManagerDiv") {
        initUserArgs();
        listUser();
    } else if (targetId === "tableListDiv") {
        initTableListArgs();
        loadTablesData();
    } else if (targetId === "userSettingDiv") {
        // 切换到点击个人设置时，清空修改密码的三个输入框和其对应的错误提示区域
        initUserSettingDiv();
    }
}

function initUserSettingDiv() {
    document.getElementById("oldPasswordSet").value = "";
    document.getElementById("newPasswordSet").value = "";
    document.getElementById("confirmPasswordSet").value = "";
    document.getElementById("passwordOldError").textContent = "";
    document.getElementById("newPasswordSetError").textContent = "";
    document.getElementById("confirmPasswordSetError").textContent = "";
}

// 获取当前登录用户名称，回显在页面右上角
loadCurrentUser();

function initModal(htmlPath, elementId) {
    // 使用 fetch 加载外部HTML
    fetch(`${htmlPath}`)
        .then(response => response.text())
        .then(data => {
            document.getElementById(`${elementId}`).innerHTML = data;
        })
        .catch(error => console.error('Error loading external HTML:', error));
}
initModal('html/usermodal.html', 'modals-user');
initModal('html/tableManagerModal.html', 'modals-tableList');
initModal('html/columnManagerModal.html', 'modals-columnManager');