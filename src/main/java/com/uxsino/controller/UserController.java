package com.uxsino.controller;

import com.uxsino.DTO.AddUserDTO;
import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.response.ResultInfo;
import com.uxsino.common.response.ResultPage;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.common.utils.PasswordEncoder;
import com.uxsino.entity.Role;
import com.uxsino.entity.User;
import com.uxsino.service.RoleService;
import com.uxsino.service.UserService;
import com.uxsino.service.WebLogService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 用户管理控制器
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 11:08
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private WebLogService webLogService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public ResultInfo listUser(PageListDTO userDTO) {
        try {
            Page<User> pageUsers = userService.listUser(userDTO);
            if (null == pageUsers) {
                return ResultInfo.failed("查询失败");
            }

            ResultPage<User> resultPage = ResultPage.restPage(pageUsers, pageUsers.getContent());
            return ResultInfo.success("查询成功", resultPage);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + Constant.LIST_USER_MANAGER + "，请求参数：" + userDTO.toString(), e);
            return ResultInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResultInfo save(@RequestBody AddUserDTO addUserDTO) {
        String addUserEvent = Constant.SAVE_USER_DATA;
        try {
            addUserEvent += OperUtils.parseArg(addUserDTO.getUserName());

            // 用户信息唯一性验证
            User byUsername = userService.findByUserName(addUserDTO.getUserName());
            if (byUsername != null) {
                return recordLogAndReturn(addUserEvent, "该用户名已经存在");
            }
            Integer roleId = addUserDTO.getRole();
            Role role = roleService.findByRoleId(roleId);
            if (role == null) {
                return recordLogAndReturn(addUserEvent, "角色不存在");
            }
            // 查询当前登录用户信息
            Subject subject = SecurityUtils.getSubject();
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
            // 封装用户信息
            Map<String, String> map = PasswordEncoder.enCodePassWord(addUserDTO.getUserName(), addUserDTO.getPassword());
            User user = new User();
            user.setUserName(addUserDTO.getUserName());
            user.setSalt(map.get(PasswordEncoder.SALT));
            user.setPassword(map.get(PasswordEncoder.PASSWORD));
            user.setRoles(new HashSet<>());
            user.getRoles().add(role);
            user.setCreateUser(loginUser.getUserName());
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());

            boolean isSuccess = userService.save(user);
            return isSuccess ? recordSuccessLogAndReturn(addUserEvent) : recordFailedLogAndReturn(addUserEvent);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + addUserEvent + "，请求参数：" + addUserDTO.toString(), e);
            return recordLogAndReturn(addUserEvent, e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResultInfo updateUser(@RequestParam Integer userId, @RequestBody AddUserDTO userDTO) {
        String updateUserEvent = Constant.UPDATE_USER_DATA;
        try {
            // 参数校验
            if (userId == null) {
                return recordLogAndReturn(Constant.UPDATE_USER_DATA, "用户ID不能为空");
            }
            User byUserId = userService.findByUserId(userId);
            if (byUserId == null) {
                return recordLogAndReturn(Constant.UPDATE_USER_DATA, "该用户不存在");
            }
            updateUserEvent = "更新用户" + OperUtils.parseArg(byUserId.getUserName()) + "的信息";
            // 登录用户信息
            Subject subject = SecurityUtils.getSubject();
            // 用户除密码之外的信息修改只能由系统管理员
            if (subject.hasRole(Constant.ADMIN) || subject.hasRole(Constant.SUPER)) {
                // 更新用户角色
                Integer roleIdOld = null;
                if (!CollectionUtils.isEmpty(byUserId.getRoles())) {
                    roleIdOld = byUserId.getRoles().iterator().next().getRoleId();
                }
                if (!userDTO.getRole().equals(roleIdOld)) {
                    Role role = roleService.findByRoleId(userDTO.getRole());
                    if (role == null) {
                        return recordLogAndReturn(updateUserEvent, "角色名不存在");
                    }
                    byUserId.getRoles().clear();
                    byUserId.getRoles().add(role);
                }
            }
            // 登录用户信息
            User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
            // 用户基本信息修改只能由用户本人或安全管理员
            if (loginUser.getUserId().equals(byUserId.getUserId()) || subject.hasRole(Constant.ADMIN)) {
                // 更新用户密码
                if (StringUtils.isNotEmpty(userDTO.getPassword())) {
                    boolean isModifyPassword = false;
                    boolean isSelf = loginUser.getUserId().equals(byUserId.getUserId());
                    // 所有用户修改自己的密码都需要原始密码
                    if (subject.hasRole(Constant.ADMIN) || subject.hasRole(Constant.SUPER) && !isSelf) {
                        isModifyPassword = true;
                    } else if (isSelf) {
                        if (PasswordEncoder.checkPassword(byUserId, userDTO.getOldPassword())) {
                            isModifyPassword = true;
                        } else {
                            return recordLogAndReturn(updateUserEvent, "用户名或原始密码不正确");
                        }
                    }

                    if (isModifyPassword) {
                        Map<String, String> map = PasswordEncoder.enCodePassWord(byUserId.getUserName(),
                                userDTO.getPassword());
                        byUserId.setSalt(map.get(PasswordEncoder.SALT));
                        byUserId.setPassword(map.get(PasswordEncoder.PASSWORD));
                    }
                }
                // 更新用户
                byUserId.setUpdateTime(new Date());
                if (userService.update(byUserId)) {
                    webLogService.success(updateUserEvent);
                    return ResultInfo.success("更新成功");
                }
                webLogService.failed(updateUserEvent);
                return ResultInfo.failed("更新失败");
            }
            webLogService.failure(updateUserEvent, "没有修改权限");
            return ResultInfo.forbidden("没有修改权限");
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + updateUserEvent + "，请求参数：userId: " + userId + ", " + userDTO.toString(), e);
            return recordLogAndReturn(updateUserEvent, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultInfo deleteUser(@RequestParam Integer recordId) {
        String deleteUserEvent = Constant.DELETE_USER_DATA + ", recordId: " + recordId;
        try {
            return delete(recordId, Constant.DELETE_USER_DATA);
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + deleteUserEvent + "，请求参数：userId: " + recordId, e);
            return recordLogAndReturn(deleteUserEvent, e.getMessage());
        }
    }

    @PostMapping("/batchModify")
    public ResultInfo batchUpdateUser(@RequestParam Integer roleId, @RequestBody List<String> recordIdList) {
        String batchUpdateUserEvent = Constant.BATCH_UPDATE_USER_DATA;
        try {
            String updateUserEvent = "批量更新用户id为" + OperUtils.parseArg(Arrays.toString(recordIdList.toArray())) + "的信息";
            boolean update = false;
            for (String userId : recordIdList) {
                // 参数校验
                if (userId == null) {
                    return recordLogAndReturn(Constant.UPDATE_USER_DATA, "用户ID不能为空");
                }
                User byUserId = userService.findByUserId(Integer.parseInt(userId));
                if (byUserId == null) {
                    return recordLogAndReturn(Constant.UPDATE_USER_DATA, "该用户不存在");
                }
                // 登录用户信息
                Subject subject = SecurityUtils.getSubject();
                // 用户除密码之外的信息修改只能由系统管理员
                if (subject.hasRole(Constant.ADMIN) || subject.hasRole(Constant.SUPER)) {
                    // 更新用户角色
                    Integer roleIdOld = null;
                    if (!CollectionUtils.isEmpty(byUserId.getRoles())) {
                        roleIdOld = byUserId.getRoles().iterator().next().getRoleId();
                    }
                    if (!roleId.equals(roleIdOld)) {
                        Role role = roleService.findByRoleId(roleId);
                        if (role == null) {
                            return recordLogAndReturn(updateUserEvent, "角色名不存在");
                        }
                        byUserId.getRoles().clear();
                        byUserId.getRoles().add(role);
                    }
                }
                // 登录用户信息
                User loginUser = (User) subject.getPrincipals().getPrimaryPrincipal();
                // 用户基本信息修改只能由用户本人或安全管理员
                if (loginUser.getUserId().equals(byUserId.getUserId()) || subject.hasRole(Constant.ADMIN)) {
                    // 更新用户
                    byUserId.setUpdateTime(new Date());
                    update = userService.update(byUserId);
                } else {
                    // 报错权限问题
                    return ResultInfo.failed("无权限");
                }
            }
            if (update) {
                webLogService.success(updateUserEvent);
                return ResultInfo.success("批量更新用户成功");
            } else {
                webLogService.failed(updateUserEvent);
                return ResultInfo.failed("批量更新用户成功");
            }
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + batchUpdateUserEvent + "，请求参数：roleId: " + roleId + ", " + recordIdList.toString(), e);
            return recordLogAndReturn(batchUpdateUserEvent, e.getMessage());
        }
    }

    @PostMapping("/batchDelete")
    public ResultInfo batchDeleteUser(@RequestBody List<String> recordIdList) {
        String batchDeleteUserEvent = Constant.BATCH_DELETE_USER_DATA + ", recordIdList: " + Arrays.toString(recordIdList.toArray());
        try {
            for (String recordId : recordIdList) {
                delete(Integer.parseInt(recordId), Constant.BATCH_DELETE_USER_DATA);
            }
            return ResultInfo.success("批量删除用户成功");
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + batchDeleteUserEvent + "，请求参数：" + recordIdList.toString(), e);
            return recordLogAndReturn(batchDeleteUserEvent, e.getMessage());
        }
    }

    @PostMapping("/setPwd")
    public ResultInfo setPwd(@RequestParam Integer userId, @RequestParam String oldPassword, @RequestParam String password) {
        String setPwdEvent = Constant.RESET_USER_PWD;
        try {
            User user = userService.findByUserId(userId);
            if (user == null) {
                return recordLogAndReturn("修改用户密码", "该用户不存在");
            }
            // 验证旧密码与当前用户使用的密码是否一致
            boolean identity = PasswordEncoder.checkPassword(user, oldPassword);
            if (!identity) {
                return ResultInfo.failed("旧密码错误");
            }

            setPwdEvent = "修改用户" + OperUtils.parseArg(user.getUserName()) + "的密码";
            Map<String, String> map = PasswordEncoder.enCodePassWord(user.getUserName(),
                    password);
            user.setSalt(map.get(PasswordEncoder.SALT));
            user.setPassword(map.get(PasswordEncoder.PASSWORD));
            user.setUpdateTime(new Date());
            if (userService.resetPwd(user)) {
                webLogService.success(setPwdEvent);
                return ResultInfo.success("修改密码成功");
            }
            webLogService.failed(setPwdEvent);
            return ResultInfo.failed("修改密码失败");
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + setPwdEvent + "，请求参数：userId: " + userId, e);
            return recordLogAndReturn(setPwdEvent, e.getMessage());
        }
    }

    @PostMapping("/resetPwd")
    public ResultInfo resetPwd(@RequestParam Integer userId, @RequestParam String password) {
        String resetPwdEvent = Constant.RESET_USER_PWD;
        try {
            User byUserId = userService.findByUserId(userId);
            if (byUserId == null) {
                return recordLogAndReturn("重置用户密码", "该用户不存在");
            }
            resetPwdEvent = "重置用户" + OperUtils.parseArg(byUserId.getUserName()) + "的密码";
            Map<String, String> map = PasswordEncoder.enCodePassWord(byUserId.getUserName(),
                    password);
            byUserId.setSalt(map.get(PasswordEncoder.SALT));
            byUserId.setPassword(map.get(PasswordEncoder.PASSWORD));
            byUserId.setUpdateTime(new Date());
            if (userService.resetPwd(byUserId)) {
                webLogService.success(resetPwdEvent);
                return ResultInfo.success("修改密码成功");
            }
            webLogService.failed(resetPwdEvent);
            return ResultInfo.failed("修改密码失败");
        } catch (Exception e) {
            // 记录log日志
            logger.error("事件：" + resetPwdEvent + "，请求参数：userId: " + userId, e);
            return recordLogAndReturn(resetPwdEvent, e.getMessage());
        }
    }

    public ResultInfo update(Integer roleId, User byUserId) {
        if (byUserId == null) {
            return recordLogAndReturn(Constant.UPDATE_USER_DATA, "该用户不存在");
        }
        String updateUserEvent = "更新用户" + OperUtils.parseArg(byUserId.getUserName()) + "的信息";
        // 登录用户信息
        Subject subject = SecurityUtils.getSubject();
        // 用户除密码之外的信息修改只能由系统管理员
        if (subject.hasRole(Constant.ADMIN) || subject.hasRole(Constant.SUPER)) {
            // 更新用户角色
            Integer roleIdOld = null;
            if (!CollectionUtils.isEmpty(byUserId.getRoles())) {
                roleIdOld = byUserId.getRoles().iterator().next().getRoleId();
            }
            if (!roleId.equals(roleIdOld)) {
                Role role = roleService.findByRoleId(roleId);
                if (role == null) {
                    return recordLogAndReturn(updateUserEvent, "角色名不存在");
                }
                byUserId.getRoles().clear();
                byUserId.getRoles().add(role);
            }
        }
        return null;
    }

    public ResultInfo delete(Integer recordId, String deleteUserEvent) {
        // 参数校验
        if (recordId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User byUserId = userService.findByUserId(recordId);
        if (byUserId == null) {
            throw new RuntimeException("用户不存在");
        }
        deleteUserEvent += OperUtils.parseArg(byUserId.getUserName());
        String roleName = null;
        if (!CollectionUtils.isEmpty(byUserId.getRoles())) {
            roleName = byUserId.getRoles().iterator().next().getRoleName();
        }
        if (Constant.ADMIN.equals(roleName)) {
            throw new RuntimeException("安全管理员不能被删除");
        }
        userService.delete(byUserId);
        return ResultInfo.success(deleteUserEvent + "成功");
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
