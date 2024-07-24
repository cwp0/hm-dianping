package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.utils
 * @Class: RefreshTokenIntercepter
 * @Description: 登录拦截器
 * @Author: cwp0
 * @CreatedTime: 2024/07/24 15:33
 * @Version: 1.0
 */
public class RefreshTokenIntercepter implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenIntercepter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

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
        // HttpSession session = request.getSession();
        // 1. 获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 2. 获取session中的用户
        // Object user = session.getAttribute("user");
        // 2. 基于token获取redis中的用户
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
        // 3. 判断用户是否存在
        if (userMap.isEmpty()) {
            return true;
        }
        // 5. 将查询到的Hash数据转为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 6. 存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);
        // 7. 刷新token有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 8. 放行
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

