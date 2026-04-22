/**
 * 公共方法
 */

// 自动附加 Token 到 Fetch 请求
(function () {
    const token = localStorage.getItem("token");

    // 如果 token 存在，重写 fetch 方法
    if (token) {
        const originalFetch = window.fetch;

        // 重写 fetch 方法，确保每次请求都会附带 token
        window.fetch = function (url, options = {}) {
            options.headers = options.headers || {};
            options.headers["token"] = token; // 将 token 添加到请求头

            return originalFetch(url, options);
        };
    }
})();

// 显示模态框的方法
function showAlert(message) {
    // 设置提示内容
    const alertMessage = document.getElementById('customAlertMessage');
    alertMessage.textContent = message;

    // 使用 Bootstrap 提供的 JavaScript API 显示模态框
    const alertModal = new bootstrap.Modal(document.getElementById('customAlertModal'));
    alertModal.show();
}

// 请求返回状态码非200的处理逻辑
function responseParse(resultCode, responseMessage, message) {
    if (resultCode === 401) {
        showAlert("登录已失效，请重新登录！");
        window.location.href = "/";  // 跳转到登录页面
    } else if (resultCode === 500) {
        showAlert(message + responseMessage);
    } else {
        showAlert(message);
    }
}

function closeModal(modalName) {
    // 关闭弹框
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById(modalName));
    modal.hide();
}

function openModal(modalName) {
    // 打开弹框
    const modal = new bootstrap.Modal(document.getElementById(modalName));
    modal.show();
}