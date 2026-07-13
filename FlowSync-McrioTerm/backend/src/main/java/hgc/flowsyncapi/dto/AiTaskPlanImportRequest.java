package hgc.flowsyncapi.dto;

import lombok.Data;
import java.util.List;

/**
 * AI 拆解任务导入请求（骨架 — 第六章跳过）
 */
@Data
public class AiTaskPlanImportRequest {
    private Long projectId;
    private Long creatorId;
    private List<AiTaskPlanItem> items;
}
