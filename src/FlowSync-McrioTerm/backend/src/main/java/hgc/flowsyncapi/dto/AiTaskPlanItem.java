package hgc.flowsyncapi.dto;

import lombok.Data;

/**
 * AI 拆解的单个任务项（骨架 — 第六章跳过）
 */
@Data
public class AiTaskPlanItem {
    private String title;
    private String description;
    private String priority;
    private Integer suggestedDays;
    private Long assigneeId;
}
