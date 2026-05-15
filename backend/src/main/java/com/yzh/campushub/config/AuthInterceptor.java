package com.yzh.campushub.config;

import com.yzh.campushub.utils.Constants;
import com.yzh.campushub.utils.JwtUtil;
import com.yzh.campushub.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 0. 放行浏览器预检请求，避免前端跨域场景被401拦截
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 获取请求头中的 Authorization
        String token = request.getHeader("Authorization");
        
        // 2. 校验token是否存在
        if (!StringUtils.hasText(token)) {
            response.setStatus(Constants.CODE_401);
            return false;
        }

        // 处理 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 3. 解析token获取userId
            Long userId = JwtUtil.getUserId(token);
            if (userId == null) {
                response.setStatus(Constants.CODE_401);
                return false;
            }
            
            // 4. 将userId存入ThreadLocal
            UserContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            response.setStatus(Constants.CODE_401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
