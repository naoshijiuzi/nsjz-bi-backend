package com.nsjz.service;

import com.nsjz.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 郭春燕
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    /**
     * 测试注册用户
     */
    @Test
    public void userRegister(){
        String userAccount = "nsjz";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        String planetCode = "2";

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);

        Assertions.assertEquals(1,result);
    }
}