package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.InviteCodeService;
import hgc.flowsyncapi.service.OperationLogService;
import hgc.flowsyncapi.service.ProjectInfoService;
import hgc.flowsyncapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final InviteCodeService inviteCodeService;
    private final OperationLogService logService;
    private final ProjectInfoService projectInfoService;

    public AdminController(UserService userService,
                           InviteCodeService inviteCodeService,
                           OperationLogService logService,
                           ProjectInfoService projectInfoService) {
        this.userService = userService;
        this.inviteCodeService = inviteCodeService;
        this.logService = logService;
        this.projectInfoService = projectInfoService;
    }

    /** 检查当前用户是否为管理员 */
    private void checkAdmin(HttpServletRequest req) {
        String role = (String) req.getAttribute("currentUserRole");
        if (!"管理员".equals(role)) {
            throw new RuntimeException("无权操作：仅管理员可执行此操作");
        }
    }

    /** 生成邀请码 */
    @PostMapping("/invite-code")
    public ApiResponse<Map<String, String>> generateInviteCode(HttpServletRequest req) {
        Long userId = AuthController.getCurrentUserId(req);
        try {
            checkAdmin(req);
            String code = inviteCodeService.generate(userId);
            logService.log(userId, "生成邀请码", "邀请码", null, "生成 2min 有效邀请码: " + code);
            Map<String, String> result = new HashMap<>();
            result.put("code", code);
            result.put("expiresIn", "120秒");
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /** 修改用户角色（升降级），降级时需指定项目接手人 */
    @PostMapping("/change-role")
    public ApiResponse<Map<String, Object>> changeRole(@RequestBody Map<String, Object> body,
                                                        HttpServletRequest req) {
        Long userId = AuthController.getCurrentUserId(req);
        try {
            checkAdmin(req);
            Long targetUserId = Long.valueOf(body.get("userId").toString());
            String newRole = body.get("role").toString();
            if (!newRole.equals("负责人") && !newRole.equals("组员")) {
                return ApiResponse.fail("无效角色：" + newRole);
            }

            User target = userService.listUsers().stream()
                    .filter(u -> u.getId().equals(targetUserId)).findFirst().orElse(null);
            if (target == null) return ApiResponse.fail("用户不存在");

            // 降级为组员时，处理其拥有的项目
            int transferred = 0;
            if ("组员".equals(newRole) && "负责人".equals(target.getRole())) {
                List<ProjectInfo> owned = projectInfoService.listOwnedProjects(targetUserId);
                if (!owned.isEmpty()) {
                    Object newOwnerObj = body.get("newOwnerId");
                    if (newOwnerObj == null) {
                        // 返回项目列表，让前端弹出选择
                        List<Map<String, Object>> projects = owned.stream().map(p -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("id", p.getId());
                            m.put("name", p.getName());
                            return m;
                        }).collect(Collectors.toList());
                        Map<String, Object> result = new HashMap<>();
                        result.put("needTransfer", true);
                        result.put("projects", projects);
                        return ApiResponse.ok("该用户拥有 " + owned.size() + " 个项目，请选择接手人", result);
                    }
                    Long newOwnerId = Long.valueOf(newOwnerObj.toString());
                    transferred = projectInfoService.transferOwnership(targetUserId, newOwnerId);
                    User newOwner = userService.listUsers().stream()
                            .filter(u -> u.getId().equals(newOwnerId)).findFirst().orElse(null);
                    logService.log(userId, "转让项目", "用户", targetUserId,
                            transferred + " 个项目转让给 " + (newOwner != null ? newOwner.getRealName() : newOwnerId));
                }
            }

            User updated = userService.changeRole(targetUserId, newRole);
            logService.log(userId, "修改角色", "用户", targetUserId,
                    "角色 → " + newRole + (transferred > 0 ? "，转让 " + transferred + " 个项目" : ""));

            Map<String, Object> result = new HashMap<>();
            result.put("user", updated);
            result.put("transferred", transferred);
            return ApiResponse.ok("操作成功" + (transferred > 0 ? "，已转让 " + transferred + " 个项目" : ""), result);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /** 获取可接手项目的候选人（其他负责人 + 管理员） */
    @GetMapping("/transfer-candidates")
    public ApiResponse<List<User>> transferCandidates(HttpServletRequest req) {
        try {
            checkAdmin(req);
            List<User> all = userService.listUsers();
            // 返回非组员的用户（负责人 + 管理员）
            List<User> candidates = all.stream()
                    .filter(u -> !"组员".equals(u.getRole()))
                    .collect(Collectors.toList());
            return ApiResponse.ok(candidates);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /** 获取全部用户（管理员视图，含 role 修改信息） */
    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers(HttpServletRequest req) {
        try {
            checkAdmin(req);
            return ApiResponse.ok(userService.listUsers());
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
