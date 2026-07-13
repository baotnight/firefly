package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hgc.flowsyncapi.entity.OperationLog;

public interface OperationLogService {
    void log(Long operatorId, String action, String targetType, Long targetId, String detail);
    Page<OperationLog> listLogs(int page, int size);
}
