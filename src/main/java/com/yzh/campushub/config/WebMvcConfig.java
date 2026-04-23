package com.yzh.campushub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor())
        .addPathPatterns("/api/**") // 拦截所有api请求
        .excludePathPatterns(
            "/api/auth/login", // 排除登录
            "/api/auth/register", // 排除注册
            "/error", // 排除错误页面
            "/api/files/**",  // 排除文件访问
            "/images/**"      // 排除静态资源
        );
    }
}

