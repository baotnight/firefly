package hgc.flowsyncapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hgc.flowsyncapi.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLog> {
}
