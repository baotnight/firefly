package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.service.OperationLogService;
import hgc.flowsyncapi.service.ProjectInfoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectInfoService projectInfoService;
    private final OperationLogService logService;

    public ProjectController(ProjectInfoService projectInfoService, OperationLogService logService) {
        this.projectInfoService = projectInfoService;
        this.logService = logService;
    }

    @GetMapping
    public ApiResponse<List<ProjectInfo>> list(HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        return ApiResponse.ok(projectInfoService.listProjects(userId));
    }

    @PostMapping
    public ApiResponse<ProjectInfo> save(@RequestBody ProjectInfo project,
                                          HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        try {
            ProjectInfo saved = projectInfoService.saveProject(project, userId);
            String action = project.getId() == null ? "创建项目" : "编辑项目";
            logService.log(userId, action, "项目", saved.getId(), "项目：" + saved.getName());
            return ApiResponse.ok(saved);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        try {
            projectInfoService.deleteProject(id, userId);
            logService.log(userId, "删除项目", "项目", id, "删除项目 ID=" + id);
            return ApiResponse.ok(null);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<Long>> body,
                                                          HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        List<Long> ids = body.get("ids");
        int count = 0;
        for (Long id : ids) {
            try {
                projectInfoService.deleteProject(id, userId);
                count++;
            } catch (RuntimeException ignored) {}
        }
        logService.log(userId, "批量删除项目", "项目", null, "删除 " + count + " 个项目");
        return ApiResponse.ok("成功删除 " + count + " 个项目", Map.of("deleted", count));
    }
}
