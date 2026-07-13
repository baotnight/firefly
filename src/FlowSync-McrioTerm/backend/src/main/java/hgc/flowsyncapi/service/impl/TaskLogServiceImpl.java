package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.TaskLogMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.TaskLogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskLogServiceImpl implements TaskLogService {

    private final TaskLogMapper taskLogMapper;
    private final TaskInfoMapper taskInfoMapper;
    private final UserMapper userMapper;

    public TaskLogServiceImpl(TaskLogMapper taskLogMapper,
                              TaskInfoMapper taskInfoMapper,
                              UserMapper userMapper) {
        this.taskLogMapper = taskLogMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<TaskLog> listTaskLogs(Long taskId) {
        QueryWrapper<TaskLog> qw = new QueryWrapper<TaskLog>().orderByDesc("id");
        if (taskId != null) {
            qw.eq("task_id", taskId);
        }
        List<TaskLog> logs = taskLogMapper.selectList(qw);
        fillNames(logs);
        return logs;
    }

    @Override
    public TaskLog saveTaskLog(TaskLog log, Long currentUserId) {
        log.setOperatorId(currentUserId);
        taskLogMapper.insert(log);
        return log;
    }

    private void fillNames(List<TaskLog> logs) {
        if (logs.isEmpty()) return;

        List<Long> taskIds = logs.stream().map(TaskLog::getTaskId).distinct().collect(Collectors.toList());
        List<Long> userIds = logs.stream().map(TaskLog::getOperatorId)
                .filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> taskNames = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<TaskInfo> tasks = taskInfoMapper.selectBatchIds(taskIds);
            for (TaskInfo t : tasks) taskNames.put(t.getId(), t.getTitle());
        }

        Map<Long, String> userNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) userNames.put(u.getId(), u.getRealName());
        }

        for (TaskLog log : logs) {
            log.setTaskTitle(taskNames.getOrDefault(log.getTaskId(), "未知"));
            log.setOperatorName(userNames.getOrDefault(log.getOperatorId(), "未知"));
        }
    }
}
