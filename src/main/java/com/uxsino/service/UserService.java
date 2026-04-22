package com.uxsino.service;

import com.uxsino.DTO.PageListDTO;
import com.uxsino.common.constant.Constant;
import com.uxsino.common.utils.LogConsts;
import com.uxsino.common.utils.OperUtils;
import com.uxsino.entity.Role;
import com.uxsino.entity.User;
import com.uxsino.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @version v1.0.0
 * Copyright 2024 Beijing Uxsino Software Co., Ltd./Branch Of Xi'an
 * All right reserved.
 * @date 2024/12/17 11:15
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public boolean save(User user) {
        // 封装为User
        return null != userRepository.save(user);
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public Page<User> listUser(PageListDTO userDTO) {

        logger.debug(Constant.LIST_USER_MANAGER + "，请求参数为：" + userDTO.toString());

        // 封装搜索字段
        PageRequest pageRequest = OperUtils.getPageRequest(userDTO, Constant.USER_MANAGER_ORDER_FIELDS);
        String[] searchFieldsArray = OperUtils.getSearchFieldsArray(userDTO.getSearchFields());
        String[] searchArray = OperUtils.getSearchArray(userDTO.getSearch());

        Page<User> page = userRepository.findAll(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                // 如果筛选条件为空或长度不匹配，返回空的筛选
                if (searchFieldsArray == null || searchArray == null
                        || searchArray.length != searchFieldsArray.length) {
                    return query.getRestriction();
                }
                for (int i = 0; i < searchFieldsArray.length; i++) {
                    String key = searchFieldsArray[i];
                    if (Constant.USER_NAME.equals(key)) {
                        // 用户名模糊匹配
                        predicates.add(builder.like(root.get(key), LogConsts.WILDCARD + searchArray[i] + LogConsts.WILDCARD));
                    } else if (Constant.ROLE_ID.equals(key) && !"0".equals(searchArray[i])) {
                        // Join users 和 roles 表
                        Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
                        // 添加 roleId 的筛选条件
                        predicates.add(builder.equal(rolesJoin.get("roleId"), searchArray[i]));
                    }
                }
                Predicate predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                return query.where(predicate).getRestriction();
            }
        }, pageRequest);

        return page;
    }

    public User findByUserId(Integer userId) {
        return userRepository.findByUserId(userId);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public boolean update(User user) {
        return null != userRepository.save(user);
    }

    public boolean resetPwd(User user) {
        return null != userRepository.save(user);
    }
}
