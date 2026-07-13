package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project_info")
public class ProjectInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String status;
    private String priority;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 非数据库字段 — 负责人姓名（联表或二次查询填充） */
    @TableField(exist = false)
    private String ownerName;
}
