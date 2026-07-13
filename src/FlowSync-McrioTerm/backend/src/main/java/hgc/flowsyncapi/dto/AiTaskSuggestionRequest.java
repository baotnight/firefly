package hgc.flowsyncapi.dto;

import lombok.Data;

/**
 * AI 单任务建议请求（骨架 — 第六章跳过）
 */
@Data
public class AiTaskSuggestionRequest {
    private String projectName;
    private String taskTitle;
    private String taskDescription;
}
