package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
        // 1. 获取session
        HttpSession session = request.getSession();
        // 2. 获取session中的用户
        Object user = session.getAttribute("user");
        // 3. 判断用户是否存在
        if (user == null) {
            // 4. 不存在，拦截
            response.setStatus(401);
            return false;
        }
        // 5. 存在，保存用户信息到ThreadLocal
        UserDTO userDTO = new UserDTO();
        // 对象属性拷贝
        BeanUtils.copyProperties(user, userDTO);
        UserHolder.saveUser(userDTO);
        // 6. 放行
        return true;
    }

    /**
     * @Description: 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     * @Param: request      {javax.servlet.http.HttpServletRequest}
     * @Param: response      {javax.servlet.http.HttpServletResponse}
     * @Param: handler      {java.lang.Object}
     * @Param: ex      {java.lang.Exception}
     * @Return: void
     * @Author: cwp0
     * @CreatedTime: 2024/7/24 16:33
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的用户信息
        UserHolder.removeUser();
    }
}

