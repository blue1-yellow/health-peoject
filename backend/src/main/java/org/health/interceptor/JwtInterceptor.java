package org.health.interceptor;

import org.health.util.JwtUtil;
import org.health.util.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 浏览器跨域预检不会携带业务 token，必须先放行，后续实际请求仍会校验 token。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 1. 获取请求头token
        String token = request.getHeader("token");
        if(token == null || token.isEmpty()){
            response.setStatus(401);
            response.getWriter().write("未登录，请先登录");
            return false;
        }
        try {
            // 2. 解析token拿到userId
            Integer userId = JwtUtil.parseToken(token);
            // 存入ThreadLocal
            UserContext.setUserId(userId);
        }catch (Exception e){
            response.setStatus(401);
            response.getWriter().write("token无效或已过期");
            return false;
        }
        // 放行
        return true;
    }

    // 请求完成后清理
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}
