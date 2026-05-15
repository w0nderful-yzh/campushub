package com.yzh.campushub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/login",
                "/api/auth/register",
                "/error",
                "/api/files/**",
                "/images/**"
            );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前端静态资源：直接匹配的文件正常返回，其余 forward 到 index.html（Vue Router history 模式）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new VueHistoryResourceResolver());
    }

    /**
     * Spring Boot 4 兼容的 ResourceResolver：静态文件存在则返回，否则返回 index.html。
     */
    private static class VueHistoryResourceResolver implements ResourceResolver {

        private static final Resource INDEX = new ClassPathResource("static/index.html");

        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                                        List<? extends Resource> locations, ResourceResolverChain chain) {
            return resolve(requestPath, locations, chain);
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                     ResourceResolverChain chain) {
            return chain.resolveUrlPath(resourcePath, locations);
        }

        private Resource resolve(String path, List<? extends Resource> locations, ResourceResolverChain chain) {
            // 先让后续 resolver 尝试直接匹配静态文件
            Resource resource = chain.resolveResource(null, path, locations);
            if (resource != null) {
                return resource;
            }
            // 匹配不到 → 返回 index.html，由 Vue Router 接管
            if (INDEX.exists()) {
                return INDEX;
            }
            return null;
        }
    }
}

