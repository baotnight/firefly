package hgc.flowsyncapi.dto;

import lombok.Data;

/**
 * AI 任务拆解请求（骨架 — 第六章跳过）
 */
@Data
public class AiTaskPlanRequest {
    private Long projectId;
    private Long operatorId;
    private String projectName;
    private String goal;
    private String description;
}
