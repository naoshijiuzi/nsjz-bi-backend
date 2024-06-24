package com.nsjz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsjz.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 27297
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-02-05 19:23:11
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    int userLogout(HttpServletRequest request);

    User getSafetyUser(User user);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);
}
