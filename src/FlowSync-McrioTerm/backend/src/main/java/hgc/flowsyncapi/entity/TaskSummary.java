package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_summary")
public class TaskSummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long taskId;
    private String summaryType;
    private String content;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ---- 非数据库字段 ----
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String taskTitle;
    @TableField(exist = false)
    private String creatorName;
}
