package com.nsjz.model.domain.request;

import lombok.Data;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 郭春燕
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -7449285166442399335L;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 校验密码
     */
    private String checkPassword;
    /**
     * 星球编号
     */
    private String planetCode;
}
