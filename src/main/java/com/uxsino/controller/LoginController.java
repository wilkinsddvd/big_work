package com.uxsino.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 10:08
 */
@Controller
public class LoginController {

    @GetMapping("/")
    public String loginPage() {
        return "login"; // 返回login.html模板
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";  // 登录成功后跳转到 home.html
    }

    @GetMapping("/tokenVerify")
    public String tokenVerify() {
        return "error";
    }
}
