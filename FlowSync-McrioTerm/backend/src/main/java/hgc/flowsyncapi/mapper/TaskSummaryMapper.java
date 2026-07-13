package hgc.flowsyncapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hgc.flowsyncapi.entity.TaskSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskSummaryMapper extends BaseMapper<TaskSummary> {
}
