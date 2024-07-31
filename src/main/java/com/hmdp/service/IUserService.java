package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    /**
     * @Description: 发送手机验证码
     * @Param: phone      {java.lang.String}
     * @Param: session      {javax.servlet.http.HttpSession}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/24 14:58
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * @Description: 登录功能
     * @Param: loginForm      {com.hmdp.dto.LoginFormDTO}
     * @Param: session      {javax.servlet.http.HttpSession}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/24 15:10
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();
}
