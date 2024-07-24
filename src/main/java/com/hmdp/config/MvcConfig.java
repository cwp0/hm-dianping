package com.hmdp.config;

import com.hmdp.utils.LoginIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginIntercepter())
                .excludePathPatterns("/user/code", "/user/login", "blog/hot", "shop/**","shop-type/**", "upload/**", "voucher/**");
    }
}

