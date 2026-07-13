package hgc.flowsyncapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hgc.flowsyncapi.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
