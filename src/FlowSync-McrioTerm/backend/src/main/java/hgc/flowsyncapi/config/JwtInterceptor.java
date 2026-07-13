package hgc.flowsyncapi.config;

import hgc.flowsyncapi.common.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 登录和注册接口不需要 Token
        String uri = request.getRequestURI();
        if (uri.contains("/api/auth/login") || uri.contains("/api/auth/register")) {
            return true;
        }

        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未登录或 Token 已过期\",\"data\":null}");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserId(token);
                String role = jwtUtils.parseToken(token).get("role", String.class);
                request.setAttribute("currentUserId", userId);
                request.setAttribute("currentUserRole", role);
                return true;
            }
        } catch (Exception ignored) {}

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"Token 无效或已过期，请重新登录\",\"data\":null}");
        return false;
    }
}
