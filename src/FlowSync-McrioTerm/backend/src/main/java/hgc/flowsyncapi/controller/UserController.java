package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.PasswordUpdateRequest;
import hgc.flowsyncapi.dto.ProfileUpdateRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.AuthService;
import hgc.flowsyncapi.service.OperationLogService;
import hgc.flowsyncapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final OperationLogService logService;

    public UserController(UserService userService, AuthService authService, OperationLogService logService) {
        this.userService = userService;
        this.authService = authService;
        this.logService = logService;
    }

    @GetMapping
    public ApiResponse<List<User>> list() {
        return ApiResponse.ok(userService.listUsers());
    }

    @PostMapping("/update-password")
    public ApiResponse<Void> updatePassword(@RequestBody PasswordUpdateRequest request,
                                             HttpServletRequest req) {
        Long userId = AuthController.getCurrentUserId(req);
        try {
            authService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
            logService.log(userId, "修改密码", "用户", userId, "修改登录密码");
            return ApiResponse.ok("密码修改成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/update-profile")
    public ApiResponse<User> updateProfile(@RequestBody ProfileUpdateRequest request,
                                            HttpServletRequest req) {
        Long userId = AuthController.getCurrentUserId(req);
        try {
            User updated = userService.updateProfile(userId, request.getPhone(), request.getEmail());
            logService.log(userId, "修改资料", "用户", userId, "更新电话/邮箱");
            return ApiResponse.ok("资料修改成功", updated);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
