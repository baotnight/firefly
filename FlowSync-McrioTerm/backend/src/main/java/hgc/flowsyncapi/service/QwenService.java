package hgc.flowsyncapi.service;

import hgc.flowsyncapi.dto.AiTaskPlanResponse;
import hgc.flowsyncapi.dto.AiTaskSuggestionRequest;
import hgc.flowsyncapi.dto.AiTaskPlanRequest;

public interface QwenService {
    /** 单任务 AI 建议 */
    String getTaskSuggestion(AiTaskSuggestionRequest request);

    /** AI 任务拆解（返回任务计划） */
    AiTaskPlanResponse generateTaskPlan(AiTaskPlanRequest request);
}
