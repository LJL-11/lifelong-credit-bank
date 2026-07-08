package org.csu.creditbank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthInterceptor(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, "未登录");
            return false;
        }

        String token = authHeader.substring(7);
        Long userId = tokenUtil.getUserId(token);
        if (userId == null) {
            writeError(response, "登录已过期，请重新登录");
            return false;
        }

        String role = tokenUtil.getRole(token);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        // /api/admin/** 需要 ADMIN 角色
        String uri = request.getRequestURI();
        if (uri.contains("/api/admin/") && !"ADMIN".equals(role)) {
            writeError(response, "权限不足，需要管理员身份");
            return false;
        }

        // 刷新 token 过期时间
        tokenUtil.refreshToken(token);
        return true;
    }

    private void writeError(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.fail(message)));
    }
}
