package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_info")
public class TaskInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long parentId;
    private String title;
    private String description;
    private Long assigneeId;
    private Long creatorId;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private String aiSuggestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ---- 非数据库字段，用于前端展示 ----
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String assigneeName;
    @TableField(exist = false)
    private String creatorName;
}
