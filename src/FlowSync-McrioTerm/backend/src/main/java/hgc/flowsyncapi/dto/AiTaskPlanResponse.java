package hgc.flowsyncapi.dto;

import lombok.Data;
import java.util.List;

/**
 * AI 任务拆解响应（骨架 — 第六章跳过）
 */
@Data
public class AiTaskPlanResponse {
    private String summary;
    private List<AiTaskPlanItem> items;
}
