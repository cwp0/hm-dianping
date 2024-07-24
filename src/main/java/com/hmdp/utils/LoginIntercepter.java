package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.utils
 * @Class: LoginIntercepter
 * @Description: 登录拦截器
 * @Author: cwp0
 * @CreatedTime: 2024/07/24 15:33
 * @Version: 1.0
 */
public class LoginIntercepter implements HandlerInterceptor {

    /**
     * @Description: 在请求处理之前进行调用（Controller方法调用之前）
     * @Param: request      {javax.servlet.http.HttpServletRequest}
     * @Param: response      {javax.servlet.http.HttpServletResponse}
     * @Param: handler      {java.lang.Object}
     * @Return: boolean
     * @Author: cwp0
     * @CreatedTime: 2024/7/24 16:33
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 判断是否需要去拦截(ThreadLocal中是否有用户信息)
        if (UserHolder.getUser() == null) {
            // 没有，，需要拦截，设置状态码
            response.setStatus(401);
            return false;
        }
        // 有用户，放行
        return true;
    }
}

