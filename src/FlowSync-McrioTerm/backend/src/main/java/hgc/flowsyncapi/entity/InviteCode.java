package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invite_code")
public class InviteCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private Long createdBy;
    private Boolean used;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
