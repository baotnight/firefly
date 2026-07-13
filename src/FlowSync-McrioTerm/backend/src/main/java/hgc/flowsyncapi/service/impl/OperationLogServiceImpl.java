package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hgc.flowsyncapi.entity.OperationLog;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.OperationLogMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper logMapper;
    private final UserMapper userMapper;

    public OperationLogServiceImpl(OperationLogMapper logMapper, UserMapper userMapper) {
        this.logMapper = logMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void log(Long operatorId, String action, String targetType, Long targetId, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        logMapper.insert(log);
    }

    @Override
    public Page<OperationLog> listLogs(int page, int size) {
        Page<OperationLog> p = new Page<>(page, size);
        Page<OperationLog> result = logMapper.selectPage(p,
                new QueryWrapper<OperationLog>().orderByDesc("create_time"));

        // 填充操作人姓名
        List<OperationLog> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> userIds = records.stream()
                    .map(OperationLog::getOperatorId).distinct()
                    .filter(id -> id != null).toList();
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> nameMap = new HashMap<>();
            for (User u : users) nameMap.put(u.getId(), u.getRealName());
            for (OperationLog r : records) {
                r.setOperatorName(nameMap.getOrDefault(r.getOperatorId(), "未知"));
            }
        }
        return result;
    }
}
