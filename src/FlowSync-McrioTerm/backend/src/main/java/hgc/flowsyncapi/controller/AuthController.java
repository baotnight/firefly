package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.common.JwtUtils;
import hgc.flowsyncapi.dto.LoginRequest;
import hgc.flowsyncapi.dto.RegisterRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.AuthService;
import hgc.flowsyncapi.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final OperationLogService logService;

    public AuthController(AuthService authService, JwtUtils jwtUtils, OperationLogService logService) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
        this.logService = logService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.login(request.getUsername(), request.getPassword());
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            logService.log(user.getId(), "登录", "用户", user.getId(), user.getRealName() + " 登录系统");
            return ApiResponse.ok("登录成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                    request.getUsername(), request.getPassword(),
                    request.getRealName(), request.getRole(),
                    request.getInviteCode());
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            logService.log(user.getId(), "注册", "用户", user.getId(), user.getRealName() + " 注册新账号");
            return ApiResponse.ok("注册成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /** 辅助方法：从请求中获取当前用户 ID */
    public static Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("currentUserId");
    }
}
