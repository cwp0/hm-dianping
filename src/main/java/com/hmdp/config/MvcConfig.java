package com.hmdp.config;

import com.hmdp.utils.LoginIntercepter;

import com.hmdp.utils.RefreshTokenIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.config
 * @Class: MvcConfig
 * @Description: 拦截器配置
 * @Author: cwp0
 * @CreatedTime: 2024/07/24 16:40
 * @Version: 1.0
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginIntercepter())
                .excludePathPatterns("/user/code", "/user/login", "blog/hot", "shop/**","shop-type/**", "upload/**", "voucher/**").order(1);
        // 刷新token拦截器
        registry.addInterceptor(new RefreshTokenIntercepter(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}

