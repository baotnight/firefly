package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.service.OperationLogService;
import hgc.flowsyncapi.service.TaskLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-logs")
public class TaskLogController {

    private final TaskLogService taskLogService;
    private final OperationLogService logService;

    public TaskLogController(TaskLogService taskLogService, OperationLogService logService) {
        this.taskLogService = taskLogService;
        this.logService = logService;
    }

    @GetMapping
    public ApiResponse<List<TaskLog>> list(@RequestParam(required = false) Long taskId) {
        return ApiResponse.ok(taskLogService.listTaskLogs(taskId));
    }

    @PostMapping
    public ApiResponse<TaskLog> save(@RequestBody TaskLog log, HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        TaskLog saved = taskLogService.saveTaskLog(log, userId);
        logService.log(userId, "记录进度", "任务", log.getTaskId(),
                "进度 " + log.getProgressPercent() + "%");
        return ApiResponse.ok(saved);
    }
}
