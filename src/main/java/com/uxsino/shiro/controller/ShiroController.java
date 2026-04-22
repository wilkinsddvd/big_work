package com.uxsino.shiro.controller;

import com.uxsino.DTO.LoginDTO;
import com.uxsino.DTO.LoginVO;
import com.uxsino.common.response.ResultInfo;
import com.uxsino.common.utils.ParamUtil;
import com.uxsino.common.utils.PasswordEncoder;
import com.uxsino.entity.User;
import com.uxsino.service.WebLogService;
import com.uxsino.shiro.service.ShiroService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class ShiroController {
    private static final Logger logger = LoggerFactory.getLogger(ShiroController.class);

    @Autowired
    private ShiroService shiroService;

    @Autowired
    private WebLogService webLogService;

    /**
     * 登录
     */
    @PostMapping(value = "/login")
    public ResultInfo login(@Validated @RequestBody LoginDTO loginDTO, BindingResult bindingResult, HttpServletResponse response) {

        // 参数校验
        ResultInfo resultInfo = ParamUtil.checkParams(bindingResult);
        if (resultInfo != null) {
            return resultInfo;
        }
        String userName = loginDTO.getUserName();
        String password = loginDTO.getPassword();

        // 用户信息
        User user = shiroService.findByUserName(userName);
        boolean identity = PasswordEncoder.checkPassword(user, password);
        // 账号不存在、密码错误
        if (!identity) {
            logger.info("user={} login failed!", userName);
            return ResultInfo.failed("账号或密码有误");
        } else {
            // 生成token，并保存到数据库
            Map<String, Object> token = shiroService.createToken(user.getUserId());
            Integer roleId = -1;
            if (!CollectionUtils.isEmpty(user.getRoles())) {
                roleId = user.getRoles().iterator().next().getRoleId();
            }
            LoginVO loginVO = new LoginVO();
            loginVO.setToken((String) token.get("token"));
            loginVO.setExpire((LocalDateTime) token.get("expire"));
            loginVO.setUserName(userName);
            loginVO.setRoleId(roleId);
            loginVO.setServerPlatform(System.getProperties().getProperty("os.name"));
            logger.info("user={} login success!", userName);
            webLogService.success(userName, "登录系统");

            return ResultInfo.success("登录成功", loginVO);
        }

    }

    /**
     * 获取当前登录用户名称
     */
    @PostMapping("/getLoginUser")
    public ResultInfo getLoginUserName() {
        try {
            Subject subject = SecurityUtils.getSubject();
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
            return ResultInfo.success("获取当前登录用户名称", loginUser);
        } catch (Exception e) {
            return ResultInfo.failed("获取当前登录用户名称失败");
        }
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    public ResultInfo logout(@RequestHeader("token") String token) {
        String operator = "user";
        try {
            Subject subject = SecurityUtils.getSubject();
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
            operator = loginUser.getUserName();
            shiroService.logout(token);
            webLogService.success(operator, "退出系统");
            return ResultInfo.success("您已安全退出系统");
        } catch (Exception e) {
            webLogService.failed(operator, "退出系统");
            return ResultInfo.failed("退出系统失败");
        }
    }
}
