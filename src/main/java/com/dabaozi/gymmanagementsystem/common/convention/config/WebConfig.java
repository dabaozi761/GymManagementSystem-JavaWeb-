package com.dabaozi.gymmanagementsystem.common.convention.config;

import com.dabaozi.gymmanagementsystem.common.convention.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//配置类,配置类需要继承WebMvcConfigurer,拦截器
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private TokenInterceptor tokenInterceptor;
    @Override
    /**
     * 添加拦截器 registry:拦截器注册类
     */
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/login");//不拦截登录接口
    }
}
