package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.service.OperationLogService;
import hgc.flowsyncapi.service.TaskSummaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/summaries")
public class TaskSummaryController {

    private final TaskSummaryService summaryService;
    private final OperationLogService logService;

    public TaskSummaryController(TaskSummaryService summaryService, OperationLogService logService) {
        this.summaryService = summaryService;
        this.logService = logService;
    }

    @GetMapping
    public ApiResponse<List<TaskSummary>> list() {
        return ApiResponse.ok(summaryService.listSummaries());
    }

    @PostMapping
    public ApiResponse<TaskSummary> save(@RequestBody TaskSummary summary, HttpServletRequest request) {
        Long userId = AuthController.getCurrentUserId(request);
        TaskSummary saved = summaryService.saveSummary(summary, userId);
        logService.log(userId, "撰写总结", "项目", summary.getProjectId(),
                summary.getSummaryType());
        return ApiResponse.ok(saved);
    }
}
