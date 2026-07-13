package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_log")
public class TaskLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Integer progressPercent;
    private String content;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ---- 非数据库字段 ----
    @TableField(exist = false)
    private String taskTitle;
    @TableField(exist = false)
    private String operatorName;
}
