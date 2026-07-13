package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.service.OperationLogService;
import hgc.flowsyncapi.service.ProjectInfoService;
import hgc.flowsyncapi.service.TaskInfoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskInfoService taskInfoService;
    private final ProjectInfoService projectInfoService;
    private final OperationLogService logService;

    public TaskController(TaskInfoService taskInfoService,
                          ProjectInfoService projectInfoService,
                          OperationLogService logService) {
        this.taskInfoService = taskInfoService;
        this.projectInfoService = projectInfoService;
        this.logService = logService;
    }

    @GetMapping
    public ApiResponse<List<TaskInfo>> list(@RequestParam(required = false) Long projectId,
                                             HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        String role = (String) request.getAttribute("currentUserRole");
        List<TaskInfo> tasks = taskInfoService.listTasks(projectId);
        // 管理员看全部；组员/负责人按数据隔离规则过滤
        if (!"管理员".equals(role)) {
            if (projectId == null || !projectInfoService.isProjectOwner(projectId, userId)) {
                tasks.removeIf(t -> !t.getAssigneeId().equals(userId) && !t.getCreatorId().equals(userId)
                        && !projectInfoService.isProjectOwner(t.getProjectId(), userId));
            }
        }
        return ApiResponse.ok(tasks);
    }

    @PostMapping
    public ApiResponse<TaskInfo> save(@RequestBody TaskInfo task, HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        String role = (String) request.getAttribute("currentUserRole");
        if (!"管理员".equals(role) && task.getProjectId() != null
                && !projectInfoService.isProjectOwner(task.getProjectId(), userId)) {
            return ApiResponse.fail("无权操作：只有项目负责人可以创建/编辑任务");
        }
        TaskInfo saved = taskInfoService.saveTask(task, userId);
        String action = task.getId() == null ? "创建任务" : "编辑任务";
        logService.log(userId, action, "任务", saved.getId(), "任务：" + saved.getTitle());
        return ApiResponse.ok(saved);
    }

    @PostMapping("/{id}/status")
    public ApiResponse<TaskInfo> updateStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body,
                                               HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        TaskInfo updated = taskInfoService.updateTaskStatus(id, body.get("status"), userId);
        logService.log(userId, "更新任务状态", "任务", id, "状态 → " + body.get("status"));
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        TaskInfo task = taskInfoService.getById(id);
        if (task == null) return ApiResponse.fail("任务不存在");
        String role = (String) request.getAttribute("currentUserRole");
        if (!"管理员".equals(role) && !projectInfoService.isProjectOwner(task.getProjectId(), userId))
            return ApiResponse.fail("无权操作：只有项目负责人可以删除任务");
        taskInfoService.deleteTask(id);
        logService.log(userId, "删除任务", "任务", id, "删除任务：" + task.getTitle());
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<Long>> body,
                                                          HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        String role = (String) request.getAttribute("currentUserRole");
        List<Long> ids = body.get("ids");
        int count = 0;
        for (Long id : ids) {
            TaskInfo task = taskInfoService.getById(id);
            if (task != null && ("管理员".equals(role) || projectInfoService.isProjectOwner(task.getProjectId(), userId))) {
                taskInfoService.deleteTask(id);
                count++;
            }
        }
        logService.log(userId, "批量删除任务", "任务", null, "删除 " + count + " 个任务");
        return ApiResponse.ok("成功删除 " + count + " 个任务", Map.of("deleted", count));
    }
}
